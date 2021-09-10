package dev.helight.hopper.external

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.Color
import java.net.URL

object ColorApi {

    fun get(color: Color) = get(color.asRGB().toString(radix = 16))
    fun get(hex: String): ColorApiResponse {
        val content = URL("https://www.thecolorapi.com/id?hex=$hex").readText()
        return Json.decodeFromString(content)
    }

    fun hexToColor(hex: String): Color {
        val stripped = hex.removePrefix("#")
        val rgb = stripped.toInt(radix = 16)
        return Color.fromRGB(rgb)
    }

}