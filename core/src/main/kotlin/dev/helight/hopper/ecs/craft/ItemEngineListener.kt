package dev.helight.hopper.ecs.craft

import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.ecs
import dev.helight.hopper.offstageAsync
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.PlayerInventory

class ItemEngineListener : BetterListener() {

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        val hopper = SpigotItem.getHopper(event.item.itemStack)
        if (hopper != null && event.entity is Player) offstageAsync {
            val player = event.entity as Player
            val ex = SpigotItem.load(event.item.itemStack, player) ?: return@offstageAsync
            println(ex)
            ecs.event(ItemPickupEvent(ex.first))
        }
    }

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        offstageAsync {
            println("Storing and removing loaded hopper items")
            event.player.inventory.filterNotNull().forEach {
                val hopper = SpigotItem.getHopper(it)
                if (hopper != null && ecs.storage.containsEntity(hopper)) {
                    SpigotItem.store(it, hopper)
                }
            }
        }
    }

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val hopper = SpigotItem.getHopper(event.itemDrop.itemStack)
        if (hopper != null) {
            ecs.event(ItemDropEvent(hopper)) {
                registerCallback().subscribe {
                    offstageAsync {
                        SpigotItem.store(event.itemDrop.itemStack, hopper)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        if (event.hasItem()) {
            val hopper = SpigotItem.getHopper(event.item!!)
            if (hopper != null) {
                offstageAsync {
                    ecs.event(ItemInteractEvent(hopper, event))
                }
            }
        }
    }

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        event.clickedInventory
        println("Drag")
        println(event.view.type)
        println(event.slotType)
        val player = event.whoClicked as Player

        if (event.slotType == InventoryType.SlotType.OUTSIDE) return

        if (event.clickedInventory is PlayerInventory) {
            println("Cursor: " + event.cursor)
            println("CurItem: " + event.currentItem)
            val cursor = event.cursor
            val cur = event.currentItem

            if (cursor != null) {
                val hopper = SpigotItem.getHopper(cursor)
                if (hopper != null) {
                    offstageAsync {
                        SpigotItem.load(cursor, player)!!
                    }
                    println("InvMove => Player")
                }
            }

            if (cur != null) {
                val hopper = SpigotItem.getHopper(cur)
                if (hopper != null) {
                    offstageAsync {
                        SpigotItem.store(cur, hopper)
                    }
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
                    offstageAsync {
                        SpigotItem.load(cur)
                    }
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