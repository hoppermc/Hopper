package dev.helight.hopper.ecs.craft

import dev.helight.hopper.EntityId
import dev.helight.hopper.ExportedEntity
import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.ComponentSerializer
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.data.ExportedEntitySnapshot
import dev.helight.hopper.ecs.event.Event
import dev.helight.hopper.ecs.event.EventCallback
import dev.helight.hopper.offstageAsync
import dev.helight.hopper.utilities.Persistence.load
import dev.helight.hopper.utilities.Persistence.store
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.util.*

@Suppress("EXPERIMENTAL_API_USAGE")
@Serializable
@SerialName("hopper:item")
data class SpigotItem(
    val itemId: String,
    val holder: String?
) {

    fun getHolder(): Player? {
        if (holder == null) return null
        return Bukkit.getPlayer(UUID.fromString(holder))
    }

    companion object {
        fun getHopper(itemStack: ItemStack): EntityId? {
            val id = itemStack.itemMeta?.persistentDataContainer?.load("HopperSpigotItem") ?: return null
            return id.toULongOrNull()
        }

        fun setup(itemStack: ItemStack, se: SpigotItem, entityId: EntityId) {
            val meta = itemStack.itemMeta!!
            meta.persistentDataContainer.store("HopperSpigotItem", entityId.toString())
            itemStack.itemMeta = meta
        }


        suspend fun store(itemStack: ItemStack, entityId: EntityId) {
            println("Snapshot Storing $entityId")
            if (!ecs.storage.containsEntity(entityId)) {
                println("Entity $entityId not loaded skipping store phase")
                return
            }
            var callback = CompletableDeferred<EventCallback>()
            ecs.event(ItemStoreEvent(entityId)) {
                callback.complete(registerCallback())
            }
            println("Awaiting Callback")
            callback.await().await()
            println("Callback arrived")
            val exported = ecs.storage.removeEntity(entityId)!!
            println("Parsing From Exported $exported")
            val snapshot = ExportedEntitySnapshot.fromExported(exported)
            println("Parse Successful")
            val meta = itemStack.itemMeta!!
            meta.persistentDataContainer.store("EcsSnapshot", Json.encodeToString(snapshot))
            itemStack.itemMeta = meta
        }

        suspend fun load(itemStack: ItemStack, holder: Player? = null): ExportedEntity? {
            return try {
                val content = itemStack.itemMeta!!.persistentDataContainer.load("EcsSnapshot")!!
                val exported = ExportedEntitySnapshot.toExported(Json.decodeFromString(content))
                val wrapped = ExportedEntityWrapper(exported)
                val si = wrapped.get<SpigotItem>()
                if (ecs.storage.containsEntity(wrapped.entityId)) {
                    println("Already loaded")
                    return null
                }
                val holderId = holder?.uniqueId.toString()
                wrapped.setInternal<SpigotItem>(si.copy(holder = holderId))
                println("Snapshot Loading ${exported.first}")
                val callback = CompletableDeferred<EventCallback>()
                ecs.event(ItemLoadEvent(exported.first, ExportedEntityWrapper(exported))) {
                    callback.complete(registerCallback())
                }
                val finalEntity = callback.await().await()
                    .get<ItemLoadEvent>().data
                println(finalEntity)
                println("Load Event has been processed injecting final item into archetype storage")
                ecs.push(finalEntity.entity)
                finalEntity.entity
            } catch (ex: Exception) {
                null
            }
        }

    }

}

class SpigotItemSerializer : ComponentSerializer {
    override fun serialize(value: Any?): String {
        return Json.encodeToString(value as SpigotItem)
    }

    override fun deserialize(data: String): Any {
        return Json.decodeFromString<SpigotItem>(data)
    }

}

data class ItemPickupEvent(
    val entityId: EntityId
) : Event

data class ItemDropEvent(
    val entityId: EntityId
) : Event

@ExperimentalUnsignedTypes
data class ItemLoadEvent(
    val entityId: EntityId,
    val data: ExportedEntityWrapper
) : Event

data class ItemStoreEvent(
    val entityId: EntityId
) : Event

data class ItemInteractEvent(
    val entityId: EntityId,
    val delegate: PlayerInteractEvent
) : Event


@Suppress("EXPERIMENTAL_API_USAGE")
class ItemJob : Job {

    @Suppress("UNREACHABLE_CODE")
    override fun execute(context: JobExecutionContext?) {
        Bukkit.getOnlinePlayers().forEach { player ->
            player.inventory.filterNotNull().forEach {
                val hopper = SpigotItem.getHopper(it)
                if (hopper != null && !ecs.storage.containsEntity(hopper)) {
                    offstageAsync {
                        println("SpigotItem $hopper is not in system, trying to load it")
                        SpigotItem.load(it, player)
                    }
                }
            }
        }
    }

}