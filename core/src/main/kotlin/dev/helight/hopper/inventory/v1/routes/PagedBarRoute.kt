package dev.helight.hopper.inventory.v1.routes

import dev.helight.hopper.api.Item
import dev.helight.hopper.inventory.v1.InteractivePoint
import dev.helight.hopper.inventory.v1.Gui
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

//TODO Implement
class PagedBarRoute(rows: Int, gui: Gui) : BarRoute(rows) {
    private val gui: Gui
    private val previous: InteractivePoint
    private val next: InteractivePoint
    private val pages: InteractivePoint
    private val right: ItemStack? = Item.builder(Material.LIME_STAINED_GLASS_PANE).name("§a->").delegate()
    private val left: ItemStack? = Item.builder(Material.RED_STAINED_GLASS_PANE).name("§c<-").delegate()
    override fun build() {
        pages.item =  Item.builder(pages.item)
            .amount(gui.currentPage() + 1)
            .delegate()
        previous.item = if (gui.currentPage() === 0) null else left
        next.item = if (gui.currentPage() >= pages() - 1) null else right
    }

    override fun get(absolute: Int, relative: Int, page: Int): InteractivePoint? {
        return super.get(absolute, relative, page)
    }

    init {
        this.gui = gui
        pages = InteractivePoint(Item.builder(ItemStack(Material.BOOK))
            .name(" ")
            .amount(gui.currentPage() + 1)
            .delegate(),
        ).apply {
            parent = gui
        }
        previous = InteractivePoint(null) {
            if (gui.currentPage() == 0) return@InteractivePoint
            gui.changeOffset(-1)
        }.apply {
            parent = gui
        }
        next = InteractivePoint(null){ inventoryClickEvent ->
            if (gui.currentPage() == pages() - 1) return@InteractivePoint
            gui.changeOffset(1)
        }.apply {
            parent = gui
        }
        addBar(4, previous)
        addBar(5, pages)
        addBar(6, next)
    }
}