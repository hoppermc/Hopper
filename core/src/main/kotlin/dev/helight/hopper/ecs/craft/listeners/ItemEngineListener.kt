package dev.helight.hopper.ecs.craft.listeners

import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.craft.BagHolder
import dev.helight.hopper.ecs.craft.Bags
import dev.helight.hopper.ecs.craft.EcsItem
import dev.helight.hopper.ecs.impl.components.BagComponent
import dev.helight.hopper.ecs.impl.events.CollectDetailedItemInfos
import dev.helight.hopper.ecs.impl.events.ItemDropEvent
import dev.helight.hopper.ecs.impl.events.ItemInteractEvent
import dev.helight.hopper.ecs.impl.events.ItemPickupEvent
import dev.helight.hopper.ecs.impl.jobs.ItemJob
import dev.helight.hopper.offstageAsync
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class ItemEngineListener : BetterListener() {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        ItemJob.quickCheckPlayer(event.player)
    }

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        if (event.entity is Player) {
            ItemJob.quickCheckPlayer(event.entity as Player)
            val hopper = EcsItem.getHopper(event.item.itemStack)
            if (hopper != null && event.entity is Player) offstageAsync {
                val player = event.entity as Player
                val ex = EcsItem.load(event.item.itemStack, player) ?: return@offstageAsync
                println(ex)
                ecs.event(ItemPickupEvent(ex.first))
            }
        }
    }

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        offstageAsync {
            println("Storing and removing loaded hopper items")
            event.player.inventory.filterNotNull().forEach {
                val hopper = EcsItem.getHopper(it)
                if (hopper != null && ecs.storage.containsEntity(hopper)) {
                    EcsItem.store(it, hopper)
                }
            }
        }
    }

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        ItemJob.quickCheckPlayer(event.player)
        val hopper = EcsItem.getHopper(event.itemDrop.itemStack)
        if (hopper != null) {
            offstageAsync {
                ecs.eventWithCallback(ItemDropEvent(hopper)).await()
                EcsItem.store(event.itemDrop.itemStack, hopper)
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.holder is BagHolder) {
            val bagHolder = event.inventory.holder as BagHolder
            val entity = ecs.get(bagHolder.bagID)!!
            val jsonString = Bags.itemStackArrayToBase64(event.inventory.contents as Array<ItemStack?>)!!
            entity.buffer().apply {
                set<BagComponent>(entity.get<BagComponent>().copy(data = jsonString))
            }.replaceEntity()
        }
    }

    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        if (event.hasItem()) {
            val hopper = EcsItem.getHopper(event.item!!)
            if (hopper != null) {
                val exported = ecs.get(hopper)!!
                ecs.directEvent(ItemInteractEvent(exported, event))
            }
        }
    }

    fun expand(event: InventoryClickEvent) {
        val hopper = EcsItem.getHopper(event.currentItem!!)!!
        val entity = ecs.get(hopper)!!
        val meta = event.currentItem!!.itemMeta!!
        val player = event.whoClicked as Player
        val collectEvent = CollectDetailedItemInfos(entity, player)
        offstageAsync {
            ecs.eventWithCallback(collectEvent).await()
            meta.lore = listOf(extExpandedHeader, *(collectEvent.lore.subList(0, collectEvent.lore.size).toTypedArray()))
            event.currentItem!!.itemMeta = meta
        }
    }

    fun collapse(event: InventoryClickEvent) {
        val meta = event.currentItem!!.itemMeta!!
        val lore = meta.lore!!
        var start = 0
        for (i in 0 until lore.size) {
            if (lore[i] == extExpandedHeader) {
                start = i
                break
            }
        }
        meta.lore = listOf(*(lore.subList(0, start).toTypedArray()), extCollapsedHeader)
        event.currentItem!!.itemMeta = meta
    }

    @EventHandler
    fun onInventoryClickEvent(event: InventoryClickEvent) {

        println("Inventory Click ${event.action} ${event.click}")
        val player = event.whoClicked as Player

        if (event.click == ClickType.SHIFT_RIGHT) {
            val hopper = EcsItem.getHopper(event.currentItem!!)
            if (hopper != null) {
                event.isCancelled = true
                println("Extend Infos!")
                if (event.currentItem!!.itemMeta!!.hasLore()) {
                    val lore = event.currentItem!!.itemMeta!!.lore!!
                    if (lore.contains(extExpandedHeader)) {
                        collapse(event)
                    } else if (lore.last() == extCollapsedHeader) {
                        expand(event)
                    }
                }
                return
            }
        }

        if (event.slotType == InventoryType.SlotType.OUTSIDE) return

        if (event.clickedInventory is PlayerInventory) {
            val cursor = event.cursor
            val cur = event.currentItem
            if (cursor != null) {
                val hopper = EcsItem.getHopper(cursor)
                if (hopper != null) {
                    offstageAsync {
                        EcsItem.load(cursor, player)!!
                    }
                }
            }

            if (cur != null) {
                val hopper = EcsItem.getHopper(cur)
                if (hopper != null) {
                    offstageAsync {
                        EcsItem.store(cur, hopper)
                    }
                }
            }
        } else {
            val cur = event.currentItem
            if (cur != null && (event.isShiftClick || event.click == ClickType.NUMBER_KEY)) {
                val hopper = EcsItem.getHopper(cur)
                if (hopper != null) {
                    offstageAsync {
                        EcsItem.load(cur)
                    }
                }
            }
        }
    }

    companion object {
        val extCollapsedHeader = "§8[Shift + Rechts §7Aufklappen§8]"
        val extExpandedHeader = "§8[Shift + Rechts §7Zuklappen§8]"
    }
}