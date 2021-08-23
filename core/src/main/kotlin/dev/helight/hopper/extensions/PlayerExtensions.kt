package dev.helight.hopper.extensions

import dev.helight.hopper.api.Item
import dev.helight.hopper.utilities.Raycast
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object PlayerExtensions {

    fun Player.raycast(player: Player, precision: Int = 5, maxDepth: Int = 32, colluding: Boolean = true): Location =
        Raycast.castColliderRaycast(player, precision, maxDepth, colluding)

    val OfflinePlayer.headItem: ItemStack
        get() {
            val item = Item(Material.PLAYER_HEAD)
            item.changeMetaFun {
                val skullMeta = it as SkullMeta
                skullMeta.owningPlayer = this
                skullMeta
            }
            return item.delegate()
        }


}