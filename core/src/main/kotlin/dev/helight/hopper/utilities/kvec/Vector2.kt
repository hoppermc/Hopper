package dev.helight.hopper.utilities.kvec

class Vector2 {
    var x: Double
    var y: Double

    constructor() {
        x = 0.0
        y = 0.0
    }

    constructor(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    fun distance(p: Vector2): Double {
        val dx = p.x - x
        val dy = p.y - y
        return Math.sqrt(dx * dx + dy * dy)
    }

    override fun toString(): String {
        return "{x=$x, y=$y}"
    }
}