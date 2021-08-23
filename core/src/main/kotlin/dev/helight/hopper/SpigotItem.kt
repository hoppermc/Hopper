package dev.helight.hopper

import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.data.ExportedEntitySnapshot
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
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.util.*

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

        @ExperimentalUnsignedTypes
        fun store(itemStack: ItemStack, entityId: EntityId) {
            println("Snapshot Storing $entityId")
            val exported = ecs.storage.removeEntity(entityId)!!
            val snapshot = ExportedEntitySnapshot.fromExported(exported)
            val meta = itemStack.itemMeta!!
            meta.persistentDataContainer.store("EcsSnapshot", Json.encodeToString(snapshot))
            itemStack.itemMeta = meta
        }

        @ExperimentalUnsignedTypes
        fun load(itemStack: ItemStack, holder: Player? = null): ExportedEntity? = try {
            val content = itemStack.itemMeta!!.persistentDataContainer.load("EcsSnapshot")!!
            val exported = ExportedEntitySnapshot.toExported(Json.decodeFromString(content))
            val wrapped = ExportedEntityWrapper(exported)
            val si = wrapped.get<SpigotItem>()
            println("SpigotItem: $si")
            val holderId = holder?.uniqueId.toString()
            wrapped.setInternal<SpigotItem>(si.copy(itemId = holderId))
            println("Snapshot Loading ${exported.first}")
            ecs.push(exported)
            exported
        } catch (ex: Exception) {
            null
        }

    }

}

class ItemJob : Job {
    override fun execute(context: JobExecutionContext?) {
        Bukkit.getOnlinePlayers().forEach { player ->
            player.inventory.filterNotNull().forEach {
                val hopper = SpigotItem.getHopper(it)
                if (hopper != null) {
                    //IS HOPPER
                    //println("Ticking $hopper itemstack")
                }
            }
        }
    }

}