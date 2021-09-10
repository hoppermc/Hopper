package dev.helight.hopper.api

import dev.helight.hopper.external.ColorApi
import org.bukkit.Color

class NameBuilder(
    var value: String = "",
    val block: NameBuilder.() -> Unit
) {
    init {
        block(this)
    }

    fun space(): NameBuilder {
        value += " "
        return this
    }

    fun reset(): NameBuilder {
        value += "§r"
        return this
    }

    fun strong(): NameBuilder {
        value += "§l"
        return this
    }

    fun italic(): NameBuilder {
        value += "§o"
        return this
    }

    fun obfuscate(): NameBuilder {
        value += "§k"
        return this
    }

    fun underline(): NameBuilder {
        value += "§n"
        return this
    }

    fun strike(): NameBuilder {
        value += "§m"
        return this
    }

    fun br(): NameBuilder {
        value += "\n"
        return this
    }

    override fun toString(): String {
        println(value.replace("§", "&"))
        return value
    }

    //<editor-fold desc="Infix Functions">
    infix fun add(string: String): NameBuilder {
        value += string
        return this
    }

    infix fun color(color: net.md_5.bungee.api.ChatColor): NameBuilder {
        value += color.toString()
        return this
    }

    infix fun color(color: Color): NameBuilder {
        value += net.md_5.bungee.api.ChatColor.of("#" + color.asRGB().toString(radix = 16))
        return this
    }

    infix fun color(char: Char): NameBuilder {
        value += "§$char"
        return this
    }

    infix fun hex(hex: String): NameBuilder {
        color(ColorApi.hexToColor(hex))
        return this
    }
    //</editor-fold>

}