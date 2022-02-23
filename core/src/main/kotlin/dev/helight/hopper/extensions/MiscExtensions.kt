package dev.helight.hopper.extensions

import kotlin.math.pow
import kotlin.math.roundToInt

object MiscExtensions {

    fun Double.toStrippedString(decimals: Int = 2, padded: Boolean = true): String {
        val multiply = 10.0.pow(decimals.toDouble())
        val stringify = ((this * multiply).roundToInt() / multiply).toString()
        val spliced = stringify.split(".")
        return when (padded) {
            false -> stringify
            true -> spliced[0] + "." + spliced[1].padEnd(decimals, '0')
        }
    }

}