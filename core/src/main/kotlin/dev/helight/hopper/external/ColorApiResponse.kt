package dev.helight.hopper.external


import dev.helight.hopper.api.Item
import dev.helight.hopper.api.NameBuilder
import dev.helight.hopper.utilities.Chat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta

@Serializable
data class ColorApiResponse(
    @SerialName("cmyk")
    val cmyk: Cmyk,
    @SerialName("contrast")
    val contrast: Contrast,
    @SerialName("_embedded")
    val embedded: Embedded,
    @SerialName("hex")
    val hex: Hex,
    @SerialName("hsl")
    val hsl: Hsl,
    @SerialName("hsv")
    val hsv: Hsv,
    @SerialName("image")
    val image: Image,
    @SerialName("_links")
    val links: Links,
    @SerialName("name")
    val name: Name,
    @SerialName("rgb")
    val rgb: Rgb,
    @SerialName("XYZ")
    val xYZ: XYZ
) {

    fun generateLore(seperate: Boolean = true): Array<String> {
        return arrayOf(
            NameBuilder{color(generateColor()).strong().add("⬛").space().add(name.value)}.toString(),
            *when(seperate) {
                true -> arrayOf("", Chat.line(24, center = " §8Additional "))
                false -> arrayOf()
            },
            "§7RGB: §c${rgb.r} §a${rgb.g} §b${rgb.b}"
                .padStart(4),
            "§7Hex: §7${hex.value}"
                .padStart(4),
            "§7Dec: §7${hex.clean.toInt(radix = 16).toString(radix = 10)}"
                .padStart(4),
        )
    }

    fun generateColor(): Color {
        return Color.fromRGB(rgb.r, rgb.g, rgb.b)
    }

    fun inkFlask(): ItemStack {
        val color = generateColor()
        return Item.builder(Material.POTION)
            .changeMeta {
                it as PotionMeta
                it.color = color
            }.name(NameBuilder {
                color(color)
                add(name.value)
                add(" ")
                add("Flask")
            }.toString()).lore(*generateLore())
            .flag(ItemFlag.HIDE_POTION_EFFECTS)
            .flag(ItemFlag.HIDE_ATTRIBUTES)
            .delegate()
    }


    @Serializable
    data class Cmyk(
        @SerialName("c")
        val c: Int,
        @SerialName("fraction")
        val fraction: Fraction,
        @SerialName("k")
        val k: Int,
        @SerialName("m")
        val m: Int,
        @SerialName("value")
        val value: String,
        @SerialName("y")
        val y: Int
    ) {
        @Serializable
        data class Fraction(
            @SerialName("c")
            val c: Double,
            @SerialName("k")
            val k: Double,
            @SerialName("m")
            val m: Double,
            @SerialName("y")
            val y: Double
        )
    }

    @Serializable
    data class Contrast(
        @SerialName("value")
        val value: String
    )

    @Serializable
    class Embedded

    @Serializable
    data class Hex(
        @SerialName("clean")
        val clean: String,
        @SerialName("value")
        val value: String
    )

    @Serializable
    data class Hsl(
        @SerialName("fraction")
        val fraction: Fraction,
        @SerialName("h")
        val h: Int,
        @SerialName("l")
        val l: Int,
        @SerialName("s")
        val s: Int,
        @SerialName("value")
        val value: String
    ) {
        @Serializable
        data class Fraction(
            @SerialName("h")
            val h: Double,
            @SerialName("l")
            val l: Double,
            @SerialName("s")
            val s: Double
        )
    }

    @Serializable
    data class Hsv(
        @SerialName("fraction")
        val fraction: Fraction,
        @SerialName("h")
        val h: Int,
        @SerialName("s")
        val s: Int,
        @SerialName("v")
        val v: Int,
        @SerialName("value")
        val value: String
    ) {
        @Serializable
        data class Fraction(
            @SerialName("h")
            val h: Double,
            @SerialName("s")
            val s: Double,
            @SerialName("v")
            val v: Double
        )
    }

    @Serializable
    data class Image(
        @SerialName("bare")
        val bare: String,
        @SerialName("named")
        val named: String
    )

    @Serializable
    data class Links(
        @SerialName("self")
        val self: Self
    ) {
        @Serializable
        data class Self(
            @SerialName("href")
            val href: String
        )
    }

    @Serializable
    data class Name(
        @SerialName("closest_named_hex")
        val closestNamedHex: String,
        @SerialName("distance")
        val distance: Double,
        @SerialName("exact_match_name")
        val exactMatchName: Boolean,
        @SerialName("value")
        val value: String
    )

    @Serializable
    data class Rgb(
        @SerialName("b")
        val b: Int,
        @SerialName("fraction")
        val fraction: Fraction,
        @SerialName("g")
        val g: Int,
        @SerialName("r")
        val r: Int,
        @SerialName("value")
        val value: String
    ) {
        @Serializable
        data class Fraction(
            @SerialName("b")
            val b: Double,
            @SerialName("g")
            val g: Double,
            @SerialName("r")
            val r: Double
        )
    }

    @Serializable
    data class XYZ(
        @SerialName("fraction")
        val fraction: Fraction,
        @SerialName("value")
        val value: String,
        @SerialName("X")
        val x: Int,
        @SerialName("Y")
        val y: Int,
        @SerialName("Z")
        val z: Int
    ) {
        @Serializable
        data class Fraction(
            @SerialName("X")
            val x: Double,
            @SerialName("Y")
            val y: Double,
            @SerialName("Z")
            val z: Double
        )
    }
}