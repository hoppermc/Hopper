package dev.helight.hopper.utilities

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Chat {
    fun send(player: Player, format: String?, vararg objects: Any?) {
        val msg = String.format(format!!, *objects)
        player.sendMessage("§8[§eSystem§8]§7 $msg")
    }

    fun send(player: Player, prefix: String, format: String?, vararg objects: Any?) {
        val msg = String.format(format!!, *objects)
        player.sendMessage("§8[§e$prefix§8]§7 $msg")
    }

    fun send(player: Player, color: String, prefix: String, format: String?, vararg objects: Any?) {
        val msg = String.format(format!!, *objects)
        player.sendMessage("§8[$color$prefix§8]§7 $msg")
    }

    fun sendAll(format: String?, vararg objects: Any?) {
        val msg = "§8[§eSystem§8]§7 ${String.format(format!!, *objects)}"
        Bukkit.getOnlinePlayers().forEach { it.sendMessage(msg) }
    }

    fun sendAll(prefix: String, format: String?, vararg objects: Any?) {
        val msg = "§8[§e$prefix§8]§7 ${String.format(format!!, *objects)}"
        Bukkit.getOnlinePlayers().forEach { it.sendMessage(msg) }
    }

    fun sendAll(color: String, prefix: String, format: String?, vararg objects: Any?) {
        val msg = "§8[$color$prefix§8]§7 ${String.format(format!!, *objects)}"
        Bukkit.getOnlinePlayers().forEach { it.sendMessage(msg) }
    }

    fun clearSend(player: Player, color: String, prefix: String, format: String?, vararg objects: Any?) {
        for (i in 0..19) {
            player.sendMessage("")
        }
        val msg = String.format(format!!, *objects)
        player.sendMessage("§8[$color$prefix§8]§7 $msg")
    }

    fun clearSend(player: Player, textComponent: TextComponent?) {
        for (i in 0..19) {
            player.sendMessage("")
        }
        player.spigot().sendMessage(textComponent!!)
    }

    fun sendActionbar(player: Player,  format: String?, vararg objects: Any?) {
        val msg = String.format(format!!, *objects)
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg.toTextComponent())
    }

    fun String.toTextComponent(): TextComponent {
        val component = TextComponent()
        TextComponent.fromLegacyText("").forEach { component.addExtra(it) }
        return component
    }
}