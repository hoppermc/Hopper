package dev.helight.hopper.ecs.craft

import dev.helight.hopper.EntityId
import dev.helight.hopper.ExportedEntity
import dev.helight.hopper.data.KeyLocks
import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.data.ExportedEntitySnapshot
import dev.helight.hopper.ecs.impl.components.RollingMetaStorage
import dev.helight.hopper.ecs.impl.events.ItemLoadEvent
import dev.helight.hopper.ecs.impl.events.ItemStoreEvent
import dev.helight.hopper.toKey
import dev.helight.hopper.utilities.Persistence.load
import dev.helight.hopper.utilities.Persistence.store
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

@Suppress("EXPERIMENTAL_API_USAGE")
@Serializable
@SerialName("hopper:item")
data class EcsItem(
    val itemId: String,
    val holder: String?
) {

    fun getHolder(): Player? {
        if (holder == null) return null
        return Bukkit.getPlayer(UUID.fromString(holder))
    }

    companion object {
        private val storeLocks = KeyLocks<EntityId>()
        private val loadLocks = KeyLocks<EntityId>()

        fun getHopper(itemStack: ItemStack): EntityId? {
            try {
                val id = itemStack.itemMeta?.persistentDataContainer?.load("HopperSpigotItem") ?: return null
                return id.toULongOrNull()
            } catch (ex: Exception) {
                ex.printStackTrace()
                return null
            }
        }

        fun isWearing(entityId: EntityId, player: Player): Boolean = player.inventory.armorContents.any {
            if (it == null) return@any false
            val hopper = getHopper(it)
            return@any hopper != null && hopper == entityId
        }

        fun allInPlayer(player: Player): List<ExportedEntityWrapper> {
            val uid = player.uniqueId.toString()
            return ecs.queryExpanded(TreeSet(setOf(EcsItem::class.java.toKey())))
                .filter {
                    it.get<EcsItem>().holder == uid
                }.toList()
        }

        fun setup(itemStack: ItemStack, se: EcsItem, entityId: EntityId) {
            val meta = itemStack.itemMeta!!
            meta.persistentDataContainer.store("HopperSpigotItem", entityId.toString())
            itemStack.itemMeta = meta
        }

        fun checkCanStore(entityId: EntityId): Boolean {
            if (storeLocks.isLocked(entityId)) {
                println("Entity $entityId is currently storage locked")
                return false
            }

            if (!ecs.storage.containsEntity(entityId)) {
                println("Entity $entityId not loaded skipping store phase")
                return false
            }
            return true
        }

        suspend fun store(itemStack: ItemStack, entityId: EntityId) {
            println("Snapshot Storing $entityId")
            if (!checkCanStore(entityId)) return
            storeLocks.lock(entityId)
            try {
                ecs.eventWithCallback(ItemStoreEvent(entityId)).await()
                println("Store event called back")
                val exported = ecs.storage.removeEntity(entityId)!!
                val snapshot = ExportedEntitySnapshot.fromExported(exported)
                val meta = itemStack.itemMeta!!
                meta.persistentDataContainer.store("EcsSnapshot", Json.encodeToString(snapshot))
                itemStack.itemMeta = meta
            } finally {
                storeLocks.unlock(entityId)
            }
        }

        suspend fun peekStore(itemStack: ItemStack, entityId: EntityId) {
            if (!checkCanStore(entityId)) return
            storeLocks.lock(entityId)
            try {
                val peek = ecs.get(entityId)!!
                if (peek.has<RollingMetaStorage>()) {
                    println("Peek Storing $entityId")
                    ecs.eventWithCallback(ItemStoreEvent(entityId)).await()
                    val exported = ecs.storage.getEntity(entityId)!!
                    val snapshot = ExportedEntitySnapshot.fromExported(exported)
                    val meta = itemStack.itemMeta!!
                    meta.persistentDataContainer.store("EcsSnapshot", Json.encodeToString(snapshot))
                    itemStack.itemMeta = meta
                }
            } finally {
                storeLocks.unlock(entityId)
            }
        }

        suspend fun load(itemStack: ItemStack, holder: Player? = null): ExportedEntity? {
            val entityId = getHopper(itemStack) ?: return null
            if (loadLocks.isLocked(entityId)) {
                println("Load is locked")
                return null
            }
            if (ecs.storage.containsEntity(entityId)) {
                println("Already loaded")
                return null
            }

            if (storeLocks.isLocked(entityId)) {
                println("Entity ${entityId} is currently storage locked > null load")
                return null
            }

            return try {
                loadLocks.lock(entityId)
                val content = itemStack.itemMeta!!.persistentDataContainer.load("EcsSnapshot")!!
                println(content)
                val exported = ExportedEntitySnapshot.toExported(Json.decodeFromString(content))
                val wrapped = ExportedEntityWrapper(exported)
                val si = wrapped.get<EcsItem>()
                val holderId = holder?.uniqueId.toString()
                wrapped.setInternal<EcsItem>(si.copy(holder = holderId))
                println("Snapshot Loading ${exported.first}")
                val finalEntity = ecs.eventWithCallback(ItemLoadEvent(exported.first, ExportedEntityWrapper(exported))).await().get<ItemLoadEvent>().data
                println("Load Event has been processed injecting final item into archetype storage")
                if (ecs.storage.containsEntity(finalEntity.entityId)) {
                    println("Already loaded => Skipping at last")
                    return null
                }
                ecs.push(finalEntity.entity)
                finalEntity.entity
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            } finally {
                loadLocks.unlock(entityId)
            }
        }
    }
}

