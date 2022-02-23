package dev.helight.hopper.api

import dev.helight.hopper.utilities.Chat
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.entity.Player
import kotlin.math.ceil

class MessageBuilder(
    var value: TextComponent = TextComponent(),
    var lineLength: Int = 80,
    val tabLength: Int = 4,
    var context: MessageContext = MessageContext(),
    block: MessageBuilder.() -> Unit,
) {
    val paragraphFormat
        get() = value.toLegacyText()

    val modifierTrimmed
        get() = Chat.analyse(value.toLegacyText()).modifierTrimmed

    val modifierReplaced
        get() = value.toLegacyText().replace("§", "&")

    val tabValue
        get() = " ".repeat(tabLength)

    init {
        block(this)
    }

    operator fun plusAssign(string: String) {
        value.addExtra(string)
    }

    operator fun plusAssign(textComponent: TextComponent) {
        value.addExtra(textComponent)
    }

    operator fun plusAssign(baseComponents: Array<BaseComponent>) {
        val component = TextComponent()
        baseComponents.forEach {
            component.addExtra(it)
        }
        value.addExtra(component)
    }

    infix fun emphasize(string: String): MessageBuilder {
        this += "§r" + context.emphasize + string
        return this
    }

    fun chatPrefix(string: String, color: String = "§c") {
        this += "§r§8[§r$color$string§r§8]§r "
    }

    fun clickable(string: String, eventValue: String, action: ClickEvent.Action = ClickEvent.Action.RUN_COMMAND, hover: String? = null,): MessageBuilder {
        val component = TextComponent(string)
        component.clickEvent = ClickEvent(action, eventValue)
        if (hover != null) component.hoverEvent =
            HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover))
        this += component
        return this
    }

    fun hover(string: String, hover: String): MessageBuilder {
        val component = TextComponent(string)
        component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover))
        this += component
        return this
    }

    fun line(length: Int = lineLength, color: String? = null, center: String = "", prefix: String? = null, suffix: String? = null): MessageBuilder {
        val previous = Chat.analyse(value.toLegacyText())
        this += Chat.line(length, color, center, prefix, suffix) + previous.total
        return this
    }

    fun color(vararg colors: ChatColor): MessageBuilder {
        this += colors.joinToString { "§${it.char}" }
        return this
    }

    fun center(string: String, color: String = "") {
        val result = Chat.analyse(color + string)
        val spacing = ceil((lineLength - result.density) / 2).toInt()
        this += "§r" + " ".repeat(spacing) + color + string + "\n"
    }

    fun tab(): MessageBuilder {
        this += tabValue
        return this
    }

    fun reset(): MessageBuilder {
        this += "§r"
        return this
    }

    fun strong(): MessageBuilder {
        this += "§l"
        return this
    }

    fun italic(): MessageBuilder {
        this += "§o"
        return this
    }

    fun obfuscate(): MessageBuilder {
        this += "§k"
        return this
    }

    fun underline(): MessageBuilder {
        this += "§n"
        return this
    }

    fun strike(): MessageBuilder {
        this += "§m"
        return this
    }

    fun br(): MessageBuilder {
        this += "\n"
        return this
    }

    fun embeded(message: String, vararg color: ChatColor): String {
        val before = Chat.analyse(value.toLegacyText())
        return "§r" + color.joinToString { "§${it.char}" } + message + "§r" + before.total
    }

    fun send(player: Player) {
        player.spigot().sendMessage(value)
    }

    fun clearSend(player: Player) {
        Chat.clearSend(player, value)
    }

    //<editor-fold desc="Infix Functions">
    infix fun add(string: String): MessageBuilder {
        this += string
        return this
    }

    infix fun addLine(string: String): MessageBuilder {
        this += string
        this += "\n"
        return this
    }

    infix fun basic(string: String): MessageBuilder {
        this += "§r" + context.color + string
        return this
    }

    infix fun color(color: ChatColor): MessageBuilder {
        add("§" + color.char)
        return this
    }

    infix fun hex(color: String): MessageBuilder {
        println(net.md_5.bungee.api.ChatColor.of(color).toString().replace("§", "&"))
        add(net.md_5.bungee.api.ChatColor.of(color).toString())
        return this
    }

    infix fun hex(color: Color): MessageBuilder {
        add(net.md_5.bungee.api.ChatColor.of("#" + color.asRGB().toString(radix = 16)).toString())
        return this
    }

    infix fun embed(message: String): MessageBuilder {
        val before = Chat.analyse(value.toLegacyText())
        return add("§r" + message + "§r" + before.total)
    }
    //</editor-fold>
}

