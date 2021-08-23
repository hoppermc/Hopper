package dev.helight.hopper

import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.ecs.ExportedEntityWrapper
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.PlayerInventory

class ItemEngineListener : BetterListener() {

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        val hopper = SpigotItem.getHopper(event.item.itemStack)
        if (hopper != null && event.entity is Player) {
            val player = event.entity as Player
            SpigotItem.load(event.item.itemStack, player) ?: return
        }
    }

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val hopper = SpigotItem.getHopper(event.itemDrop.itemStack)
        if (hopper != null) {
            SpigotItem.store(event.itemDrop.itemStack, hopper)
        }
    }

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        event.clickedInventory
        println("Drag")
        println(event.view.type)
        val player = event.whoClicked as Player

        if (event.clickedInventory is PlayerInventory) {
            println("Cursor: " + event.cursor)
            println("CurItem: " + event.currentItem)
            val cursor = event.cursor
            val cur = event.currentItem

            if (cursor != null) {
                val hopper = SpigotItem.getHopper(cursor)
                if (hopper != null) {
                    ExportedEntityWrapper(SpigotItem.load(cursor, player)!!)
                    println("InvMove => Player")
                }
            }

            if (cur != null) {
                val hopper = SpigotItem.getHopper(cur)
                if (hopper != null) {
                    SpigotItem.store(cur, hopper)
                    println("InvMove <= Player")
                }
            }

        } else {
            println("Cursor: " + event.cursor)
            println("CurItem: " + event.currentItem)
            val cur = event.currentItem
            if (cur != null && (event.isShiftClick || event.click == ClickType.NUMBER_KEY)) {
                val hopper = SpigotItem.getHopper(cur)
                if (hopper != null) {
                    SpigotItem.load(cur)
                }
            }
            /*
            val hopper = SpigotItem.getHopper(event.item)
            if (hopper != null) {
                SpigotItem.store(event.item, hopper)
            }
            */
            println("InvMove <= Player")
        }
    }
}