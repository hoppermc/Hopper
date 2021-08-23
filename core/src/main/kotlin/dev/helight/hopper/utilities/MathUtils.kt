package dev.helight.hopper.utilities

import org.bukkit.Location
import java.util.regex.Pattern

object MathUtils {
    fun contains(polygon: List<Location>?, location: Location): Boolean {
        var i: Int
        var j: Int
        var result = false
        /*
        OptionalDouble ymin = polygon.stream().mapToDouble(x -> x.getY()).min();
        OptionalDouble ymax = polygon.stream().mapToDouble(x -> x.getY()).max();
        if (!ymin.isPresent() || !ymax.isPresent()) return false;
        if (!(location.getY() >= ymin.getAsDouble() && location.getY() <= ymax.getAsDouble())) return false;
        */i = 0
        j = polygon!!.size - 1
        while (i < polygon.size) {
            if (polygon[i].z > location.z != polygon[j].z > location.z &&
                location.x < (polygon[j].x - polygon[i].x) * (location.z - polygon[i].z) / (polygon[j].z - polygon[i].z) + polygon[i].x
            ) {
                result = !result
            }
            j = i++
        }
        return result
    }

    fun containsBox(a: Location, b: Location, location: Location): Boolean {
        return location.x >= a.x && location.x <= b.x &&
                location.y >= a.y && location.y <= b.y &&
                location.z >= a.z && location.z <= b.z
    }

    fun toDoubleString(number: Double, decimal: Int): String {
        val multiply = Math.pow(10.0, decimal.toDouble())
        val rounded = Math.round(number * multiply) / multiply
        val s = java.lang.Double.toString(number)
        val strings = s.split(Pattern.quote(".").toRegex()).toTypedArray()
        val dec = StringBuilder(strings[1])
        while (dec.length < decimal) {
            dec.append("0")
        }
        return strings[0] + "." + dec.toString()
    }
}