package dev.helight.hopper.utilities

import com.comphenix.protocol.utility.MinecraftReflection
import dev.helight.hopper.hopper
import dev.helight.hopper.offstageAsync
import kotlinx.coroutines.delay
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.time.Duration
import kotlin.math.ceil
import kotlin.text.set

object Chat {

    val colors = mutableSetOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    val operators = mutableSetOf('k', 'l', 'm', 'n', 'o', 'r')
    val validCodes = colors + operators + setOf('x')

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

    fun showWorldTitle(player: Player, location: Location, msg: String, centered: Boolean = true, duration: Duration = Duration.ZERO): IntArray {
        val lines = msg.replace("\r", "").split("\n").reversed()
        val height = (Constants.NAMETAG_HEIGHT * lines.size) + (lines.size * Constants.NAMETAG_GAP_OFFSET)
        val basePosition = when (centered) {
            true -> location.clone().subtract(0.0, (height / 2.0), 0.0)
            false -> location.clone()
        }
        val entityIds = lines.mapIndexed { index, line ->
            hopper.spigot.proto.spawnWorldString(player, basePosition.clone()
                .add(0.0, index * Constants.NAMETAG_HEIGHT, 0.0), line)
        }.toIntArray()

        if (duration != Duration.ZERO) {
            offstageAsync {
                delay(duration.toMillis())
                hopper.spigot.proto.destroyEntity(player, *entityIds)
            }
        }

        return entityIds
    }

    fun String.toTextComponent(): TextComponent {
        val component = TextComponent()
        TextComponent.fromLegacyText(this).forEach { component.addExtra(it) }
        return component
    }

    fun String.toChatComponentText(): Any {
        return MinecraftReflection.getChatComponentTextClass().getConstructor(String::class.java).newInstance(this)
    }

    fun analyse(message: String): MessageAnalyseResult {
        val indices = mutableListOf<Int>()
        var color = 'f'
        var isBold = false
        var isStrikeThrough = false
        var isUnderlined = false
        var isMagic = false
        var isItalic = false
        var validChars = 0
        var density = 0.0

        var skipNext = false
        message.mapIndexed { index, c ->
            if (skipNext) skipNext = false
            else if (c == '§' && index < message.length - 1 && validCodes.contains(message[index + 1])) {
                when (val code = message[index + 1]) {
                    in colors -> color = code
                    'r' -> {
                        color = 'f'
                        isBold = false
                        isStrikeThrough = false
                        isUnderlined = false
                        isMagic = false
                        isItalic = false
                    }
                    'l' -> isBold = true
                    'm' -> isStrikeThrough = true
                    'n' -> isUnderlined = true
                    'k' -> isMagic = true
                    'o' -> isItalic = true
                    'x' -> {} //Ignore Hex Color Modifier
                    else -> error("Invalid code")
                }
                skipNext = true
                validChars += 2
                indices.add(index)
            } else {
                density += when(c) {
                    ' ', 'i', 'l', 't', 'I', '|' -> 1.0
                    else -> 1.4
                } * when(isBold) {
                    true -> 1.125
                    false -> 1.0
                }
            }
        }

        val modifiers =  when(isBold) {
            true -> "§l"
            false -> ""
        } + when(isStrikeThrough) {
            true -> "§m"
            false -> ""
        } + when(isUnderlined) {
            true -> "§n"
            false -> ""
        } + when(isMagic) {
            true -> "§k"
            false -> ""
        } + when(isItalic) {
            true -> "§o"
            false -> ""
        }

        println("Density: $density")

        return MessageAnalyseResult(
            delegate = message,
            color = "§$color",
            modifiers = modifiers,
            total = "§$color$modifiers",
            validCodeChars = validChars,
            codeIndices = indices,
            density = density
        )
    }

    fun line(length: Int = 20, color: String? = null, center: String = "", prefix: String? = null, suffix: String? = null): String {
        val centerResult = analyse(center)
        val freeSpace = length - centerResult.density
        val rawLeft = ceil(freeSpace / 2.0).toInt()
        val left = when(prefix) {
            null -> rawLeft
            else -> rawLeft - 1
        }

        val right = when(suffix) {
            null -> (freeSpace - rawLeft).toInt()
            else -> (freeSpace - rawLeft).toInt() - 1
        }

        val linePrefix = "§r" + when(color) {
            null -> "§8"
            else -> "$color"
        } + "§m"



        val value = (prefix ?: "") +  linePrefix + " ".repeat(left) + "§r" + center + linePrefix + " ".repeat(right) + (suffix ?: "")
        println(value.replace("§", "&"))
        return value
    }

    data class MessageAnalyseResult(
        val delegate: String,
        val color: String,
        val modifiers: String,
        val total: String,
        val validCodeChars: Int,
        val codeIndices: List<Int>,
        val density: Double
    ) {
        val visibleChars: Int
            get() = delegate.length - validCodeChars

        val modifierTrimmed: String
            get() {
                val work = StringBuilder(delegate)
                codeIndices.forEach {
                    work[it] = '⌫'
                    work[it + 1] = '⌫'
                }
                return work.toString().replace("⌫", "")
            }
    }
}