package dev.helight.hopper.api

import dev.helight.hopper.api.TextComponents.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import java.awt.Color

typealias ComponentPreset = TextComponent.() -> Unit

fun textComponent() = TextComponents.create()

val basicPreset: ComponentPreset = {
    color = ChatColor.of(Color(0xf4f4f4))
    isBold = false
}

val strongPreset: ComponentPreset = {
    color = ChatColor.of(Color(0xffffff))
    isBold = true
}

val secondaryTextPreset: ComponentPreset = {
    color = ChatColor.of(Color(0xc6c6c6))
    isBold = false
}

val linkPreset: ComponentPreset = {
    color = ChatColor.of(Color(0x78a9ff))
    isBold = false
}

val errorPreset: ComponentPreset = {
    color = ChatColor.of(Color(0xfa4d56))
}

val warningPreset: ComponentPreset = {
    color = ChatColor.of(Color(0xf1c21b))
}
val infoPreset: ComponentPreset = {
    color = ChatColor.of(Color(0x4589ff))
}

val successPreset: ComponentPreset = {
    color = ChatColor.of(Color(0x42be65))
}

object TextComponents {

    fun create(block: TextComponent.() -> Unit = {}): TextComponent {
        val component = TextComponent()
        block(component)
        return component
    }

    fun combined(vararg components: TextComponent): TextComponent {
        val component = TextComponent()
        components.forEach {
            component.addExtra(it)
        }
        return component
    }

    fun chatPrefix(string: String, color: String = "§c") = legacy( "§r§8[§r$color$string§r§8]§r ")

    fun legacy(text: String): TextComponent = create {
        TextComponent.fromLegacyText(text).forEach {
            addExtra(it)
        }
    }

    fun text(text: String, color: ChatColor): TextComponent = create {
        this.text = text
        this.color = color
    }

    fun text(text: String, color: java.awt.Color): TextComponent = create {
        this.text = text
        this.color = ChatColor.of(color)
    }

    infix fun TextComponent.with(text: String): TextComponent {
        this.text += text
        return this
    }

    infix fun TextComponent.extra(text: String): TextComponent {
        this.addExtra(text)
        return this
    }

    infix fun TextComponent.spaced(text: String): TextComponent {
        this.text += " $text"
        return this
    }

    infix fun TextComponent.add(text: String): TextComponent {
        this.text += text
        return this
    }

    infix fun TextComponent.with(color: ChatColor): TextComponent {
        this.color = color
        return this
    }

    infix fun TextComponent.with(textMod: TextMod): TextComponent {
        textMod.modifier(this)
        return this
    }

    infix fun TextComponent.padStart(length: Int): TextComponent {
        val additional = length - toPlainText().length
        if (additional > 0) {
            this.text = " ".repeat(additional) + this.text
        }
        return this
    }

    infix fun TextComponent.padEnd(length: Int): TextComponent {
        val additional = length - toPlainText().length
        if (additional > 0) {
            this.addExtra(" ".repeat(additional))
        }
        return this
    }


    infix fun TextComponent.with(preset: ComponentPreset): TextComponent {
        preset(this)
        return this
    }

}

enum class TextMod(
    val modifier: TextComponent.() -> Unit
) {
    NEWLINE({
        add("\n")
    }),
    SPACE({
        add(" ")
    }),
    TAB({
        add("    ")
    }),

    BOLD({
        isBold = true
    }),
    ITALIC({
        isItalic = true
    }),
    MAGIC({
        isObfuscated = true
    }),
    UNDERLINE({
       isUnderlined = true
    }),
    STRIKE_THROUGH({
        isStrikethrough = true
    })
}