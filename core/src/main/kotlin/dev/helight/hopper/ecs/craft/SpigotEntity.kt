package dev.helight.hopper.entity

import dev.helight.hopper.EntityId
import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.data.ExportedEntitySnapshot
import dev.helight.hopper.ecs.system.HopperSystem
import dev.helight.hopper.ecs.system.HopperSystemOptions
import dev.helight.hopper.toKey
import dev.helight.hopper.utilities.Persistence.load
import dev.helight.hopper.utilities.Persistence.store
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.util.BoundingBox
import java.util.*

@Serializable
@SerialName("minecraft:entity")
data class SpigotEntity(
    val entityId: String,

    @Transient
    var eventPassed: Boolean = false
) {
    fun resolve(): Entity? {
        return getFast(entityId)
    }

    fun isCurrentlyLoaded(): Boolean {
        val resolved = getFast(entityId) ?: return false
        println("Resolved successfully checking validity")
        return resolved.isValid
    }

    companion object {
        val entityCache = WeakHashMap<String, EntityResult>()
        val globalEntityCache: HashMap<String, List<Entity>> = HashMap()

        fun getNearbyEntities(location: Location, radius: Double): List<Entity> {
            val queryBox = BoundingBox.of(location.clone().subtract(radius, radius, radius), location.clone().add(radius, radius, radius))
            return globalEntityCache.get(location.world!!.name)!!.filter {
                it.boundingBox.overlaps(queryBox)
            }.toList()
        }

        fun getFast(entityId: String): Entity? {
            val result = entityCache[entityId]
            if (result == null) {
                val e = Bukkit.getEntity(UUID.fromString(entityId))
                if (e == null || !e.isValid) entityCache[entityId] = EntityResult(false, null)
                else entityCache[entityId] = EntityResult(true, e)
                return getFast(entityId)
            }
            if (!result.exists) return null
            return result.entity!!
        }

        fun invalidateEntity(entity: Entity) = entityCache.remove(entity.uniqueId.toString())

        fun invalidateEntity(entityId: String) = entityCache.remove(entityId)

        @ExperimentalUnsignedTypes
        fun delete(entityId: String) {
            ecs.query(SpigotEntity::class.java).firstOrNull { it.get<SpigotEntity>().entityId == entityId }?.apply {
                ecs.deleteEntity(this@apply.entityId)
            }
        }

        fun forEntity(entity: Entity): SpigotEntity = SpigotEntity(entity.uniqueId.toString())

        fun getHopper(entity: Entity): EntityId? {
            val id = entity.persistentDataContainer.load("HopperSpigotEntity") ?: return null
            return id.toULongOrNull()
        }

        fun setup(entity: Entity, se: SpigotEntity, entityId: EntityId) {
            entity.persistentDataContainer.store("HopperSpigotEntity", entityId.toString())
        }

        @ExperimentalUnsignedTypes
        fun store(entity: Entity, entityId: EntityId) {
            println("Snapshot Storing $entityId")
            val exported = ecs.storage.removeEntity(entityId)!!
            val snapshot = ExportedEntitySnapshot.fromExported(exported)
            entity.persistentDataContainer.store("EcsSnapshot", Json.encodeToString(snapshot))
        }

        @ExperimentalUnsignedTypes
        fun load(entity: Entity) {
            val content = entity.persistentDataContainer.load("EcsSnapshot")!!
            val exported = ExportedEntitySnapshot.toExported(Json.decodeFromString(content))
            println("Snapshot Loading ${exported.first}")
            ecs.push(exported)
        }
    }
}

data class EntityResult(
    val exists: Boolean = false,
    val entity: Entity? = null
)

@ExperimentalUnsignedTypes
class SpigotEntitySystem : HopperSystem(sortedSetOf(SpigotEntity::class.java.toKey()), HopperSystemOptions(isTicking = true)) {

    override fun tickIndividual(wrapper: ExportedEntityWrapper) {
        val entityComponent = wrapper.get<SpigotEntity>()
        if (entityComponent.isCurrentlyLoaded()) {
            println("System ticking for ${entityComponent.entityId}")
        } else {
            println("Entity ${entityComponent.entityId} is not loaded")
        }
    }
}
