package dev.helight.hopper.ecs

import dev.helight.hopper.*
import dev.helight.hopper.data.ArchetypeStorage
import dev.helight.hopper.data.CborSingleEntityRepository
import dev.helight.hopper.data.Snowflake
import dev.helight.hopper.ecs.data.EcsSnapshot
import dev.helight.hopper.ecs.event.Event
import dev.helight.hopper.ecs.event.EventCallback
import dev.helight.hopper.ecs.event.HopperEvent
import dev.helight.hopper.ecs.event.HopperEventHandler
import dev.helight.hopper.ecs.system.HopperSystem
import dev.helight.hopper.ecs.system.HopperSystemTicker
import dev.helight.hopper.registry.SimpleRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import org.quartz.Job
import org.quartz.JobBuilder.newJob
import org.quartz.JobExecutionContext
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.streams.toList

@Suppress("EXPERIMENTAL_API_USAGE")
class HopperECS {

    private val snowflake = Snowflake()
    private val incrementalIdCounter = AtomicLong(0)

    var storage = ArchetypeStorage()
    val systems = SimpleRegistry<HopperSystem>()
    val eventHandlers = SimpleRegistry<HopperEventHandler>()
    val serializers = ConcurrentHashMap<ULong, ComponentSerializer>()
    val classMapping = ConcurrentHashMap<String, Class<*>>()

    val composer = Composer()

    internal fun start() {
        if (hopper.coreConfiguration.persistEcs) loadSnapshot()
        hopper.schedule(
            newJob(HopperSystemTicker::class.java)
                .withIdentity("systemTicker", "hopper")
                .build(), hopper.getBukkitLikeTrigger("systemTickerTrigger", "hopper")
        )

        if (hopper.coreConfiguration.persistEcs && hopper.coreConfiguration.backupJob) hopper.schedule(
            newJob(EcsBackupJob::class.java)
                .withIdentity("ecsBackup", "hopper")
                .build(),
            newTrigger()
                .withIdentity("ecsBackupTrigger", "hopper")
                .withSchedule(
                    simpleSchedule()
                        .repeatForever()
                        .withIntervalInMinutes(1)
                ).startAt(Date.from(Instant.now().plus(Duration.ofSeconds(15)))).build()
        )
    }

    internal fun stop() {
        if (hopper.coreConfiguration.persistEcs) storeSnapshot()
        systems.all().forEach { it.stop() }
        systems.truncate()
    }

    fun storeSnapshot() {
        val snapshot = EcsSnapshot(peekNewestId(), storage.backup())
        CborSingleEntityRepository.write(snapshot)
    }

    fun loadSnapshot(): Boolean {
        val snapshot = CborSingleEntityRepository.read<EcsSnapshot>() ?: return false
        storage = ArchetypeStorage.restore(snapshot.storeSnapshot)
        setNewestId(snapshot.idCounterSnapshot)
        return true
    }

    fun peekNewestId(): ULong = incrementalIdCounter.get().toULong()

    fun setNewestId(newId: ULong) = incrementalIdCounter.set(newId.toLong())


    inline fun <reified T : HopperSystem> system() {
        val system = T::class.java.newInstance()
        if (systems.containsValue(system)) error("System already registered")
        systems.register(system)
        system.start()
    }

    inline fun <reified T : HopperEventHandler> handler() {
        val handler = T::class.java.newInstance()
        if (eventHandlers.containsValue(handler)) error("EventHandler already registered")
        eventHandlers.register(handler)
    }

    inline fun <reified T> serializer(serializer: ComponentSerializer) {
        serializers[T::class.java.toKey()] = serializer
        classMapping[T::class.java.name] = T::class.java
    }

    fun create(block: suspend BufferedEntity.() -> Unit) = offstageAsync {
        val buffered = BufferedEntity()
        block(buffered)
        buffered.push()
    }

    suspend fun createSuspended(block: suspend BufferedEntity.() -> Unit) = coroutineScope {
        val buffered = BufferedEntity()
        block(buffered)
        buffered.push()
        buffered.entity
    }


    fun createEntityWithOperation(block: (suspend EntityPipelineContext.() -> Unit)? = null): EntityId {
        val id = newEntityId()
        storage.addEntity(id)
        if (block != null) operation {
            block(EntityPipelineContext(id))
        }
        return id
    }

    fun deleteEntity(id: EntityId) {
        storage.removeEntity(id)
    }

    fun push(exportedEntity: ExportedEntity) {
        storage.addEntity(exportedEntity.first, exportedEntity.second, exportedEntity.third.toMutableList())
    }

    inline fun <reified T> add(id: EntityId, value: Any? = null) {
        val componentId = T::class.java.toKey()
        if (!serializers.containsKey(componentId)) registerDefaultSerializerForClass<T>()
        storage.addComponent(id, componentId, value)
    }

    inline fun <reified T> set(id: EntityId, value: Any? = null) {
        val componentId = T::class.java.toKey()
        if (!serializers.containsKey(componentId)) registerDefaultSerializerForClass<T>()
        storage.updateComponent(id, componentId, value)
    }

    inline fun <reified T> tag(id: EntityId) {
        if (!T::class.java.isAnnotationPresent(TagComponent::class.java)) error("Class must be annotated with @TagComponent")
        val value = T::class.java.newInstance()
        add<T>(id, value)
    }

    inline fun <reified T> remove(id: EntityId) {
        val componentId = T::class.java.toKey()
        storage.removeComponent(id, componentId)
    }

    inline fun <reified T : Event> event(event: T, noinline block: suspend EventPipelineContext<T>.() -> Unit = {}) =
        operation {
            val entity = createEntityWithOperation()
            tag<TransientEntity>(entity)
            add<T>(entity, event)
            block(EventPipelineContext(entity))
            tag<HopperEvent>(entity)
        }

    inline fun <reified T : Event> event(noinline block: suspend EventPipelineContext<T>.() -> Unit = {}) = operation {
        val entity = createEntityWithOperation()
        tag<TransientEntity>(entity)
        tag<T>(entity)
        block(EventPipelineContext(entity))
        tag<HopperEvent>(entity)
    }

    suspend inline fun <reified T : Event> eventWithCallback(noinline block: suspend BufferedEventPipelineContext<T>.() -> Unit = {}): EventCallback {
        val deferred = CompletableDeferred<EventCallback>()
        create {
            tag<TransientEntity>()
            tag<HopperEvent>()
            tag<T>()
            val callback = EventCallback()
            block(BufferedEventPipelineContext(this))
            deferred.complete(callback)
        }
        return deferred.await()
    }

    fun query(vararg classes: Class<*>): List<ExportedEntityWrapper> {
        val componentGroup = TreeSet(classes.map { it.toKey() })
        return storage.queryEntities(componentGroup).map {
            ExportedEntityWrapper(Triple(it.first, componentGroup, it.second))
        }.toList()
    }

    fun query(componentGroup: ComponentGroup): List<ExportedEntityWrapper> = storage.queryEntities(componentGroup).map {
        ExportedEntityWrapper(Triple(it.first, componentGroup, it.second))
    }.toList()

    fun queryExpanded(componentGroup: ComponentGroup): List<ExportedEntityWrapper> = storage.queryEntitiesExpanded(componentGroup).map {
        ExportedEntityWrapper(Triple(it.first, componentGroup, it.second))
    }.toList()

    fun get(id: EntityId): ExportedEntityWrapper? {
        if (!storage.containsEntity(id)) return null
        val entity = storage.getEntity(id) ?: return null
        return ExportedEntityWrapper(entity)
    }

    internal fun newEntityId(): ULong {
        return when (hopper.coreConfiguration.ecsEntityIdGenerationStrategy) {
            "incremental" -> incrementalIdCounter.getAndIncrement().toULong()
            "snowflake" -> snowflake.nextId().toULong()
            else -> error("Unsupported id generation strategy")
        }
    }

    inline fun <reified T> registerDefaultSerializerForClass() {
        println("Registering default gson serializer for component ${T::class.java.toKey()}[${T::class.java.name}]")
        ecs.serializer<T>(GsonComponentSerializer(T::class.java))
    }

    fun registerDefaultSerializerForClass(clazz: Class<*>): ComponentSerializer {
        println("Registering default gson serializer for component ${clazz}[${clazz.name}]")
        val serializer = GsonComponentSerializer(clazz)
        ecs.serializers[clazz.toKey()] = serializer
        return serializer
    }

    fun querySystems(componentGroup: ComponentGroup): List<HopperSystem> = systems.stream().filter {
        it.componentGroup.containsAll(componentGroup)
    }.toList()

    fun operation(block: suspend () -> Unit) = offstageAsync {
        HopperSystemTicker.startOperation()
        try {
            block()
        } finally {
            HopperSystemTicker.stopOperation()
        }
    }

}

@ExperimentalUnsignedTypes
data class EventPipelineContext<T>(val entityId: EntityId) {

    fun registerCallback(): EventCallback {
        val callback = EventCallback()
        ecs.add<EventCallback>(entityId, callback)
        return callback
    }

}

@ExperimentalUnsignedTypes
data class BufferedEventPipelineContext<T>(val entity: BufferedEntity)

data class EntityPipelineContext(val entityId: EntityId)

@ExperimentalUnsignedTypes
@ExperimentalSerializationApi
class EcsBackupJob : Job {

    override fun execute(context: JobExecutionContext) {
        println("Backing up ECS System")
        val t1 = Instant.now()
        ecs.storeSnapshot()
        val t2 = Instant.now()
        println("Backup completed in ${Duration.between(t1, t2).toMillis()}ms")
    }

}

