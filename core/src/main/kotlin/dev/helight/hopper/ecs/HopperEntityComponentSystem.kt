package dev.helight.hopper.ecs

import dev.helight.hopper.*
import dev.helight.hopper.data.CborSingleEntityRepository
import dev.helight.hopper.data.Snowflake
import dev.helight.hopper.ecs.data.EcsCompose
import dev.helight.hopper.ecs.data.EcsSnapshot
import dev.helight.hopper.registry.SimpleRegistry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.quartz.Job
import org.quartz.JobBuilder.newJob
import org.quartz.JobExecutionContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.streams.toList

@ExperimentalUnsignedTypes
class HopperEntityComponentSystem {

    private val snowflake = Snowflake()
    private val incrementalIdCounter = AtomicLong(0)

    var storage = ArchetypeStorage()
    val systems = SimpleRegistry<HopperSystem>()
    val serializers = ConcurrentHashMap<ULong, ComponentSerializer>()
    val classMapping = ConcurrentHashMap<String, Class<*>>()

    val composer = Composer()

    @ExperimentalSerializationApi
    internal fun start() {
        if (hopper.coreConfiguration.persistEcs) loadSnapshot()
        hopper.schedule(newJob(HopperSystemTicker::class.java)
            .withIdentity("systemTicker", "hopper")
            .build(), hopper.getPerSecondTrigger("systemTickerTrigger", "hopper"))
    }

    @ExperimentalSerializationApi
    internal fun stop() {
        if (hopper.coreConfiguration.persistEcs) storeSnapshot()
        systems.all().forEach { it.stop() }
        systems.truncate()
    }

    @ExperimentalSerializationApi
    fun storeSnapshot() {
        val snapshot = EcsSnapshot(peekNewestId(), storage.backup())
        CborSingleEntityRepository.write(snapshot)
    }

    @ExperimentalSerializationApi
    fun loadSnapshot(): Boolean {
        val snapshot = CborSingleEntityRepository.read<EcsSnapshot>() ?: return false
        storage = ArchetypeStorage.restore(snapshot.storeSnapshot)
        setNewestId(snapshot.idCounterSnapshot)
        return true
    }

    fun peekNewestId(): ULong = incrementalIdCounter.get().toULong()

    fun setNewestId(newId: ULong) = incrementalIdCounter.set(newId.toLong())


    inline fun <reified T: HopperSystem> system() {
        val system = T::class.java.newInstance()
        if (systems.containsValue(system)) error("System already registered")
        systems.register(system)
        system.start()
    }

    inline fun <reified T> serializer(serializer: ComponentSerializer) {
        serializers[T::class.java.toKey()] = serializer
        classMapping[T::class.java.name] = T::class.java
    }

    fun createEntity(): EntityId {
        val id = newEntityId()
        storage.addEntity(id)
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

    inline fun <reified T> remove(id: EntityId) {
        val componentId = T::class.java.toKey()
        storage.removeComponent(id, componentId)
    }

    fun query(vararg classes: Class<*>): List<ExportedEntityWrapper>
    {
        val componentGroup = TreeSet(classes.map { it.toKey() })
        return storage.queryEntities(componentGroup).map {
            ExportedEntityWrapper(Triple(it.first, componentGroup, it.second))
        }.toList()
    }

    fun query(componentGroup: ComponentGroup): List<ExportedEntityWrapper> = storage.queryEntities(componentGroup).map {
        ExportedEntityWrapper(Triple(it.first, componentGroup, it.second))
    }.toList()

    fun get(id: EntityId): ExportedEntityWrapper? {
        if (!storage.containsEntity(id)) return null
        val entity = storage.getEntity(id) ?: return null
        return ExportedEntityWrapper(entity)
    }

    internal fun newEntityId(): ULong {
        return when(hopper.coreConfiguration.ecsEntityIdGenerationStrategy) {
            "incremental" -> incrementalIdCounter.getAndIncrement().toULong()
            "snowflake" -> snowflake.nextId().toULong()
            else -> error("Unsupported id generation strategy")
        }
    }

    inline fun <reified T> registerDefaultSerializerForClass() {
        println("Registering default gson serializer for component ${T::class.java.toKey()}[${T::class.java.name}]")
        ecs.serializer<T>(GsonComponentSerializer(T::class.java))
    }

    fun querySystems(componentGroup: ComponentGroup): List<HopperSystem> = systems.stream().filter {
        it.componentGroup.containsAll(componentGroup)
    }.toList()

}



@ExperimentalUnsignedTypes
@ExperimentalSerializationApi
class EcsBackupJob : Job {

    override fun execute(context: JobExecutionContext) {
        ecs.storeSnapshot()
    }

}

object ComponentGroupSerializer : KSerializer<ComponentGroup> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ecs:componentGroup", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ComponentGroup {
        return TreeSet(decoder.decodeString().split("-").map {
            it.toULong()
        })
    }
    override fun serialize(encoder: Encoder, value: ComponentGroup) {
        encoder.encodeString(value.joinToString("-") { it.toString() })
    }
}

class Composer {

    @ExperimentalUnsignedTypes
    fun executeCompose(content: String): EntityId {
        val compose = Json.decodeFromString<EcsCompose>(content)
        val components = compose.parseComponents()
        val entity = ecs.createEntity()
        components.forEach {
            ecs.storage.addComponent(entity, it.first, it.second)
        }
        return entity
    }

}