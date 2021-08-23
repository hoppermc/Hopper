package dev.helight.hopper.inventory.debug

import dev.helight.hopper.api.Item
import dev.helight.hopper.inventory.InteractivePoint
import dev.helight.hopper.inventory.routes.PagedBarRoute
import dev.helight.hopper.inventory.Gui
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class DebugGui : Gui() {
    override fun construct() {
        addNode("root",
            0,
            InteractivePoint(ItemStack(Material.DIAMOND)) { event -> changeView("paged") })

            .addNode("root",
                8,
                InteractivePoint.nextPage(ItemStack(Material.OAK_BUTTON)))
            .addNode("root",
                1,
                0,
                InteractivePoint.previousPage(ItemStack(Material.OAK_BUTTON)))
            .addNode("root",
                1,
                8,
                InteractivePoint.switchView(ItemStack(org.bukkit.Material.ANVIL), "secondary"))
            .addNode("secondary",
                0,
                InteractivePoint(ItemStack(Material.GOLD_INGOT))
            )
        val route = PagedBarRoute(rows, this)
        for (i in 0..254) {
            val stack: ItemStack =
                Item.builder(Material.GOLD_INGOT).name(i.toString() + "").delegate()
            route.put(i, InteractivePoint(stack))
        }
        views.put("paged", route)
        createInventory()
    }
}