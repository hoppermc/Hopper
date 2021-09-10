package dev.helight.hopper.inventory.v1.impl

import dev.helight.hopper.inventory.v1.InteractivePoint
import dev.helight.hopper.inventory.v1.Route

class DefaultRouteImpl : Route {
    private val map: MutableMap<Int, InteractivePoint> = HashMap()
    override fun asMap(): MutableMap<Int, InteractivePoint> {
        return map
    }

    override fun build() {}
}