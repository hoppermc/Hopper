package dev.helight.hopper

import dev.helight.hopper.data.*
import dev.helight.hopper.data.configurations.HopperConfiguration
import dev.helight.hopper.data.configurations.HopperDataConfiguration
import dev.helight.hopper.data.repositories.MongoCrudRepository
import dev.helight.hopper.ecs.ComponentSerializer
import dev.helight.hopper.ecs.HopperECS
import kotlinx.serialization.Serializable
import org.bukkit.plugin.java.JavaPlugin
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.quartz.*
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import java.util.*
import kotlin.reflect.KClass

/**
 * Java class derived id for a component class or tag.
 */
typealias ComponentID = ULong

/**
 * Pseudo-Random id either generated as a snowflake or
 * through an incremental counter.
 */
typealias EntityId = ULong

typealias TypeGroup = List<Class<*>>

/**
 * Sorted set of component ids, used as a key for [Archetypes][dev.helight.hopper.data.Archetype]
 * and all connected systems.
 */
typealias ComponentGroup = TreeSet<ComponentID>

typealias ComponentData = Any?

typealias FilledComponent = Pair<ComponentID, ComponentData>

typealias AssociatedEntity = Pair<EntityId, Archetype>

/**
 * All necessary data to import/export data from/to [Archetypes][dev.helight.hopper.data.Archetype]
 */
typealias ExportedEntity = Triple<EntityId, ComponentGroup, MutableList<ComponentData>>

/**
 * Component Transformers are a very critical part for **altering component values** in a reactive way
 * **without losing state**. If multiple transformers want to modify a component at the same time,
 * they will execute after one another, while an individual transformer is executing, the **archetype is write-locked**,
 * which makes it very important to **minimize the execution time** as much as possible.
 *
 * @author HelightDev
 * @sample dev.helight.hopper.ecs.craft.HealthAmountTransformer
 */
typealias ComponentTransformer = (EntityId, ComponentID, ComponentData) -> ComponentData

@HopperDsl
val ecs = HopperECS()

@HopperDsl
val hopper = HopperEngine()

/**
 * Marks a method as being part of hopper dsl.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@DslMarker
annotation class HopperDsl

/**
 * Can be used by plugins and codegenerators to generate onLoad hooks.
 * Annotated classes will be constructed via a no-args constructor.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AnnotatedPluginRegistrant

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HopperComponentId(val id: Int)

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HopperComponentClass(val clazz: KClass<*>)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AutoComponent(
    val serializer: KClass<*> = ComponentSerializer::class
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AutoConfigurePlugin

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class TagComponent

@TagComponent
class TransientEntity {}


class HopperSpigotHook : JavaPlugin() {

    companion object {
        lateinit var plugin: HopperSpigotHook
    }

    init {
        plugin = this
    }

    override fun onEnable() {
        hopper.start()
    }

    override fun onDisable() {
        hopper.stop()
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
class HopperEngine {

    private lateinit var schedulerFactory: SchedulerFactory
    lateinit var scheduler: Scheduler

    lateinit var coreConfiguration: HopperConfiguration
    lateinit var dataConfiguration: HopperDataConfiguration

    lateinit var mongoClient: CoroutineClient
    lateinit var mongoDatabase: CoroutineDatabase

    lateinit var spigot: HopperSpigot
    var isShutdown = false

    val pluginLoader = HopperSuperClassLoader()

    fun start() {
        isShutdown = false
        setupScheduler()

        coreConfiguration = ConfigurationSource.registry.findById("JsonFile")!!
            .getConfiguration(HopperConfiguration::class.java)
        dataConfiguration = ConfigurationSource.registry.findById("JsonFile")!!
            .getConfiguration(HopperDataConfiguration::class.java)

        spigot = HopperSpigot()
        spigot.hookSerializers()
        pluginLoader.loadAll()

        ecs.start()
        spigot.hook()
        if (dataConfiguration.mongo) setupMongo()

        pluginLoader.enableAll()
    }

    private fun setupScheduler() {
        schedulerFactory = StdSchedulerFactory()
        scheduler = schedulerFactory.scheduler
        scheduler.start()
    }

    private fun setupMongo() {
        System.setProperty("org.litote.mongo.test.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")
        mongoClient = KMongo.createClient(dataConfiguration.mongoConnectionString).coroutine
        mongoDatabase = mongoClient.getDatabase(dataConfiguration.mongoDatabase)
        RepositoryFactory.suppliers["mongo"] = object : RepositorySupplier {
            override fun <T : PersistentEntity> supply(type: Class<T>): SimpleCrudRepository<T> {
                return MongoCrudRepository(type)
            }
        }
    }

    fun schedule(job: JobDetail, trigger: Trigger) {
        scheduler.scheduleJob(job, trigger)
    }

    fun stop() {
        isShutdown = true
        spigot.unhook()
        ecs.stop()
        scheduler.shutdown()
    }

    fun getHighFrequencyTrigger(name: String, group: String): Trigger = newTrigger()
        .startNow()
        .withIdentity(name, group)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .repeatForever()
            .withIntervalInMilliseconds(40))
        .build()

    fun getBukkitLikeTrigger(name: String, group: String): Trigger = newTrigger()
        .startNow()
        .withIdentity(name, group)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .repeatForever()
            .withIntervalInMilliseconds(50))
        .build()

    fun getPerSecondTrigger(name: String, group: String): Trigger = newTrigger()
        .startNow()
        .withIdentity(name, group)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .repeatForever()
            .withIntervalInSeconds(1))
        .build()

}

interface HopperPlugin {

    fun load()
    fun enable()
    fun disable()

}

@Serializable
data class HopperPluginConfiguration(
    val id: String,
    val namespace: String,
    val mainClass: String,
    val loadPriority: Int = 0
)