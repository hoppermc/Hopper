package dev.helight.hopper.inventory.impl

import dev.helight.hopper.inventory.InteractivePoint
import dev.helight.hopper.inventory.Route

class DefaultRouteImpl : Route {
    private val map: MutableMap<Int, InteractivePoint> = HashMap()
    override fun asMap(): MutableMap<Int, InteractivePoint> {
        return map
    }

    override fun build() {}
}