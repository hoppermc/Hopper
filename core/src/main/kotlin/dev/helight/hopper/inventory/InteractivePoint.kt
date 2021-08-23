package dev.helight.hopper.inventory

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class InteractivePoint(
    var item: ItemStack? = null,
    var event: Consumer<InventoryClickEvent> = Consumer { ignored: InventoryClickEvent -> }
) {
    lateinit var parent: Gui

    protected fun callEvent(event: InventoryClickEvent) {
        parent!!.notifyAction()
        this.event.accept(event)
    }

    fun build() {}
    fun scheduleRebuild() {
        parent.render()
    }

    companion object {
        fun nextPage(itemStack: ItemStack): InteractivePoint {
            val itemNode = InteractivePoint(itemStack)
            itemNode.event = Consumer { ignored -> itemNode.parent.changeOffset(1) }
            return itemNode
        }

        fun previousPage(itemStack: ItemStack): InteractivePoint {
            val itemNode = InteractivePoint(itemStack)
            itemNode.event = Consumer { ignored -> itemNode.parent.changeOffset(-1) }
            return itemNode
        }

        fun switchView(itemStack: ItemStack, view: String): InteractivePoint {
            val itemNode = InteractivePoint(itemStack)
            itemNode.event = Consumer { ignored -> itemNode.parent.changeView(view) }
            return itemNode
        }
    }
}