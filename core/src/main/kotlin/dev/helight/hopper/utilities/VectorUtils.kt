package dev.helight.hopper.utilities

import org.bukkit.Location
import org.bukkit.util.Vector

//Just don't ask why this class exists and why some methods are so useless. Just kept them in for backwards-compatibility
object VectorUtils {

    /**
     * Calculates the distance per axis between two vectors and returns the highest distance
     */
    fun maxVectorDistance(a: Vector, b: Vector): Double {
        val an = a.normalize()
        val bn = b.normalize()
        val xDif = Math.abs(bn.x - an.x)
        val yDif = Math.abs(bn.y - an.y)
        val zDif = Math.abs(bn.z - an.z)
        return Math.max(Math.max(xDif, yDif), zDif)
    }

    /**
     * Distance between two locations as Vector
     */
    fun between(a: Location, b: Location): Vector {
        return b.toVector().subtract(a.toVector())
    }
}