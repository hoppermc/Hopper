package dev.helight.hopper.inventory.v1

import dev.helight.hopper.api.Item
import dev.helight.hopper.offstageAsync
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.function.BiConsumer
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

        fun suspended(initial: ItemStack? = loadingItem.clone(),
                      event: BiConsumer<Boolean, InventoryClickEvent> = BiConsumer { ignored: Boolean, InventoryClickEvent -> },
                      block: suspend InteractivePoint.() -> ItemStack?): InteractivePoint {
            var isLoaded = false
            val point = InteractivePoint(initial) {
                event.accept(isLoaded, it)
            }
            offstageAsync {
                point.item = block(point)
                isLoaded = true
                point.scheduleRebuild()
            }
            return point
        }

        val loadingItem = Item(Material.CLOCK)
            .name("§8Loading...")
            .delegate()
    }
}