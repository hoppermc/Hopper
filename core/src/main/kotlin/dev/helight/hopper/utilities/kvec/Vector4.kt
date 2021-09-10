package dev.helight.hopper.utilities.kvec

class Vector4 {
    var x: Double
    var y: Double
    var z: Double
    var w: Double

    constructor() {
        x = 0.0
        y = 0.0
        z = 0.0
        w = 0.0
    }

    constructor(x: Double, y: Double, z: Double, w: Double) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    override fun toString(): String {
        return "{x=$x, y=$y, z=$z, w=$w}"
    }
}