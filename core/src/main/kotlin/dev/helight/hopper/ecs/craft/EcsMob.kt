package dev.helight.hopper.ecs.craft

import dev.helight.hopper.EntityId
import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.data.ExportedEntitySnapshot
import dev.helight.hopper.utilities.Persistence.load
import dev.helight.hopper.utilities.Persistence.store
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.util.BoundingBox
import java.util.*

@Serializable
@SerialName("hopper:mob")
data class EcsMob(
    val entityId: String,

    @Transient
    var eventPassed: Boolean = false
) {

    fun resolve(): Entity? {
        return getFast(entityId)
    }

    fun isCurrentlyLoaded(): Boolean {
        val resolved = getFast(entityId) ?: return false
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

        fun getGlobal(entityId: String): Entity? {
            globalEntityCache.forEach { world ->
                world.value.forEach {
                    if (it.uniqueId.toString() == entityId) return it
                }
            }
            println("Didn't find entity $entityId")
            return null
        }

        fun getFast(entityId: String): Entity? {
            val result = entityCache[entityId]
            if (result == null) {
                val e = getGlobal(entityId)
                println("Found $e")
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
            ecs.query(EcsMob::class.java).firstOrNull { it.get<EcsMob>().entityId == entityId }?.apply {
                ecs.deleteEntity(this@apply.entityId)
            }
        }

        fun forEntity(entity: Entity): EcsMob = EcsMob(entity.uniqueId.toString())

        fun getHopper(entity: Entity): EntityId? {
            val id = entity.persistentDataContainer.load("HopperSpigotEntity") ?: return null
            return id.toULongOrNull()
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
            val content = entity.persistentDataContainer.load("EcsSnapshot") ?: run {
                println("Skipping load because no snapshot is present")
                return
            }
            val exported = ExportedEntitySnapshot.toExported(Json.decodeFromString(content))
            println("Snapshot Loading Entity ${exported.first}")
            ecs.push(exported)
        }
    }
}

data class EntityResult(
    val exists: Boolean = false,
    val entity: Entity? = null
)

