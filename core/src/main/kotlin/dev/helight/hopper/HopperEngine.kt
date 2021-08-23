package dev.helight.hopper

import dev.helight.hopper.data.*
import dev.helight.hopper.data.configurations.HopperConfiguration
import dev.helight.hopper.data.configurations.HopperDataConfiguration
import dev.helight.hopper.data.repositories.MongoCrudRepository
import dev.helight.hopper.ecs.Archetype
import dev.helight.hopper.ecs.HopperComponentClass
import dev.helight.hopper.ecs.HopperEntityComponentSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.bukkit.plugin.java.JavaPlugin
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.quartz.*
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.*

typealias ComponentID = ULong
typealias EntityId = ULong

typealias RegistryID = UUID

typealias TypeGroup = List<Class<*>>

typealias ComponentGroup = TreeSet<ComponentID>
typealias ComponentData = Any?
typealias FilledComponent = Pair<ComponentID, ComponentData>
typealias AssociatedEntity = Pair<EntityId, Archetype>
typealias ExportedEntity = Triple<EntityId, ComponentGroup, MutableList<ComponentData>>

object AssociatedEntityExtensions {}
object ExportedEntityExtensions {}

@ExperimentalUnsignedTypes
val ecs = HopperEntityComponentSystem()

@OptIn(ExperimentalUnsignedTypes::class)
val hopper = HopperEngine()

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@DslMarker
annotation class HopperDsl()

@OptIn(ExperimentalUnsignedTypes::class)
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

@ExperimentalUnsignedTypes
class HopperEngine {

    private lateinit var schedulerFactory: SchedulerFactory
    private lateinit var scheduler: Scheduler

    lateinit var coreConfiguration: HopperConfiguration
    lateinit var dataConfiguration: HopperDataConfiguration

    lateinit var mongoClient: CoroutineClient
    lateinit var mongoDatabase: CoroutineDatabase

    lateinit var spigot: HopperSpigot

    val pluginLoader = HopperClassLoader()

    fun start() {
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

    @HopperDsl
    fun exampleDslFunction(block: HopperEngine.() -> Unit) {
        block()
    }

    fun schedule(job: JobDetail, trigger: Trigger) {
        scheduler.scheduleJob(job, trigger)
    }

    @ExperimentalSerializationApi
    fun stop() {
        scheduler.shutdown()
        spigot.unhook()
        ecs.stop()
    }

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

@ExperimentalUnsignedTypes
fun main() {
    /*
      println(String::class.java.toKey())
      println(mutableListOf(String::class.java, Int::class.java).toKey())
      println(bitIdGen(String::class.java.hashCode(), 0U, 1U))
      println("****")
      val storage = ArchetypeStorage()
      val g1: ComponentGroup = sortedSetOf(0UL, 1UL, 2UL)
      val g2: ComponentGroup = sortedSetOf(2UL, 0UL)
      val e1 = 100UL
      val e2 = 200UL
      val archetype = storage.getArchetype(g1)
      val secondArchetype = storage.getArchetype(g2)

      archetype.push(e1, mutableListOf("1", "2", "3"))
      archetype.push(e2, mutableListOf("A", "B", "C"))
      println(archetype.get(e1, 1UL))
      println(archetype.get(e2, 1UL))
      println("--")
      archetype.print()
      println("--")
      val exported = archetype.pop(e1)!!
      archetype.print()
      println("--")
      println("Expo: $exported")
      println("===")
      secondArchetype.print()
      println("---")
      secondArchetype.push(e1, g1.migrateTo(g2, exported.third, mutableListOf()).toMutableList())
      secondArchetype.print()
      println("---")
      println(storage.queryEntities(sortedSetOf(0UL, 2UL)))
      println(storage.queryEntityIds(0UL, 1UL))
      println("---")
      println(storage.queryEntities(g2))
      val g2Projection = sortedSetOf(0UL)
      println(storage.queryEntities(g2Projection))
      println(storage.getEntity(e2))
      println("===")
      storage.addComponent(e2, 3UL, "D")
      println(storage.getEntity(e2))
      storage.removeComponent(e2, 1UL)
      println(storage.getEntity(e2))


     */

    hopper.start()


    HopperClassLoader().loadJar(File("example-1.0-SNAPSHOT.jar"))

    val e1 = ecs.createEntity()
    ecs.add<AType>(e1, "Null")
    ecs.add<BType>(e1, 0.0)

    val e2 = ecs.createEntity()
    ecs.add<AType>(e2, "Eins")
    ecs.add<BType>(e2, 1.0)

    println(ecs.storage.mappedArchetypes)

    ecs.query(AType::class.java, BType::class.java).forEach {
        println(it.entity)
        println(it.getAs<AType, Any>())
        println(it.getAs<BType, Any>())
    }

    println("===")

    println(ecs.get(e1)!!.data)
    println(ecs.get(e2)!!.project<ABProjection>())
    println(ecs.get(e2)!!.project<ABDataProjection>())

    hopper.stop()
}

interface HopperPlugin {

    fun load()
    fun enable()
    fun disable()

}


@Serializable
data class HopperPluginConfiguration(
    val mainClass: String
)

class ExampleJob : Job {

    companion object {
        var lastJobExecution = Instant.now()
    }

    override fun execute(context: JobExecutionContext) {
        val timestamp = Instant.now()
        val timeBetween = Duration.between(lastJobExecution, timestamp).toMillis()
        lastJobExecution = timestamp
        println("[${timestamp}] Time since last execution $timeBetween")
    }
}

class ABProjection {

    @HopperComponentClass(AType::class)
    lateinit var a: String

    @HopperComponentClass(BType::class)
    var b: Double = -1.0

    override fun toString(): String {
        return "ABProjection(a='$a', b=$b)"
    }

}

data class ABDataProjection(

    @HopperComponentClass(AType::class)
    var a: String,

    @HopperComponentClass(BType::class)
    var b: Double
)

class AType
class BType