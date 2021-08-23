package dev.helight.hopper.inventory.routes

import dev.helight.hopper.inventory.InteractivePoint
import dev.helight.hopper.inventory.Route
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class BarRoute(private val rows: Int) : Route {
    private val map: MutableMap<Int, InteractivePoint> = ConcurrentHashMap<Int, InteractivePoint>()
    private val bar: MutableMap<Int, InteractivePoint> = ConcurrentHashMap<Int, InteractivePoint>()

    //Exclusive
    private val maxContent: Int
    fun addBar(i: Int, node: InteractivePoint) {
        bar[i] = node
    }

    override fun asMap(): MutableMap<Int, InteractivePoint> {
        return map
    }

    override fun put(i: Int, node: InteractivePoint) {
        map[i] = node
    }

    open override operator fun get(absolute: Int, relative: Int, page: Int): InteractivePoint? {
        if (relative > maxContent) return bar[relative - maxContent]
        val offset = page * (rows - 1) * 9
        return map[offset + relative]
    }

    fun pages(): Int {
        val set: TreeSet<Int> = TreeSet<Int>(map.keys)
        val highestIndex: Int = set.last()
        return Math.ceil(highestIndex / ((rows - 1) * 9).toDouble()).toInt()
    }

    init {
        maxContent = (rows - 1) * 9 - 1
    }
}