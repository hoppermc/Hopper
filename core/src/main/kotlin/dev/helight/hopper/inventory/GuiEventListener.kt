package dev.helight.hopper.inventory

import dev.helight.hopper.api.BetterListener
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent

class GuiEventListener : BetterListener() {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.holder == null) return
        if (event.inventory.holder !is Gui) return
        event.isCancelled = true
        val gui = event.inventory.holder as Gui?
        gui!!.notifyAction()
        if (event.clickedInventory == null) return
        if (event.clickedInventory!!.holder !is Gui) return
        val slot = gui.findRelativeByActual(event.slot)
        val node = gui.getNode(slot)
        node?.event?.accept(event)
    }

    @EventHandler
    fun onInventoryMove(event: InventoryMoveItemEvent) {
        if (event.source.holder == null) if (event.source.holder !is Gui) {
            val gui = event.source.holder as Gui?
            gui!!.notifyAction()
            event.isCancelled = true
        }
        if (event.destination.holder == null) if (event.destination.holder !is Gui) {
            val gui = event.destination.holder as Gui?
            gui!!.notifyAction()
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.holder == null) return
        if (event.inventory.holder !is Gui) return
        val gui = event.inventory.holder as Gui?
        gui!!.dispose(Gui.DisposeReason.NATURAL)
    }
}