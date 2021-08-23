package dev.helight.hopper.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import dev.helight.hopper.DebugComponent
import dev.helight.hopper.api.Item
import dev.helight.hopper.hopper
import dev.helight.hopper.inventory.debug.DebugGui
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player


@ExperimentalUnsignedTypes
@CommandAlias("hopper")
class HopperEngineCommand : BaseCommand() {

    @Subcommand("debugInventory")
    fun debugInventory(player: Player) {
        val gui = DebugGui()
        gui.construct()
        gui.show(player)
    }

    @Subcommand("entity")
    fun spawnEntity(player: Player) {
        hopper.spigot.spawnEntity(player.location, EntityType.ZOMBIE)
    }

    @Subcommand("item")
    fun getItem(player: Player) {
        val item = Item.builder(Material.APPLE).name("§cHopperApple").delegate()
        hopper.spigot.convertToHopper(item)
        hopper.spigot.addComponentToItem<DebugComponent>(item, DebugComponent(name = "HopperItem"))
        player.location.world!!.dropItem(player.location, item)
    }

}