package dev.helight.hopper.ecs.craft

import dev.helight.hopper.EntityId
import dev.helight.hopper.ecs.event.DirectEventHandle
import dev.helight.hopper.ecs.impl.components.BagComponent
import dev.helight.hopper.ecs.impl.events.ItemInteractEvent
import org.bukkit.Bukkit
import org.bukkit.event.block.Action
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException


object Bags {
    fun newFromSize(title: String, rows: Int) = BagComponent(title, rows, "",)

    @Throws(IOException::class)
    fun itemStackArrayFromBase64(data: String?): Array<ItemStack?>? {
        return try {
            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            val items = arrayOfNulls<ItemStack?>(dataInput.readInt())

            // Read the serialized inventory
            for (i in items.indices) {
                items[i] = dataInput.readObject() as ItemStack?
            }
            dataInput.close()
            items
        } catch (e: ClassNotFoundException) {
            throw IOException("Unable to decode class type.", e)
        }
    }

    @Throws(IllegalStateException::class)
    fun itemStackArrayToBase64(items: Array<ItemStack?>): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)

            // Write the size of the inventory
            dataOutput.writeInt(items.size)

            // Save every element in the list
            for (i in items.indices) {
                dataOutput.writeObject(items[i])
            }

            // Serialize that array
            dataOutput.close()
            Base64Coder.encodeLines(outputStream.toByteArray())
        } catch (e: Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }
    }
}

class BagHandler : DirectEventHandle<ItemInteractEvent>(1, ItemInteractEvent::class.java) {

    override fun handle(event: ItemInteractEvent) {
        val entity = event.entity
        if (entity.has<BagComponent>() && (event.delegate.action == Action.RIGHT_CLICK_AIR || event.delegate.action == Action.RIGHT_CLICK_BLOCK)) {
            event.delegate.isCancelled = true
            val bag = entity.get<BagComponent>()
            val holder = BagHolder(entity.entityId)
            var inv = Bukkit.createInventory(holder, bag.rows * 9, bag.title)
            holder.inv = inv
            inv.setContents(
                when(bag.data == "") {
                    false -> Bags.itemStackArrayFromBase64(bag.data)!!
                    true -> arrayOfNulls(bag.rows * 9)
                }
            )
            event.delegate.player.openInventory(inv)
        }
    }
}

class BagHolder(
    val bagID: EntityId,
) : InventoryHolder {
    lateinit var inv: Inventory
    override fun getInventory(): Inventory = inv
}