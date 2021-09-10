package dev.helight.hopper.utilities

import dev.helight.hopper.extensions.VLBExtensions.destructible
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

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

    fun scalar(a: Vector, b: Vector) = (a.x * b.x) + (a.y * b.y) + (a.z * b.z)

    fun replaceFirstNotZero(vector: Vector, value: Double): Vector = when {
        vector.x == 0.0 && vector.y == 0.0 -> Vector(vector.x, vector.y, value)
        vector.x == 0.0 && vector.z == 0.0 -> Vector(vector.x, value, vector.z)
        vector.y == 0.0 && vector.z == 0.0 -> Vector(value, vector.y, vector.z)
        else -> error("All not zero")
    }

    fun replaceZero(origin: Vector, a: Location, b: Location): Location = when {
        origin.x == 0.0 && origin.y == 0.0 -> Location(a.world, a.x, a.y, b.z)
        origin.x == 0.0 && origin.z == 0.0 -> Location(a.world, a.x, b.y, a.z)
        origin.y == 0.0 && origin.z == 0.0 -> Location(a.world, b.x, a.y, a.z)
        else -> error("All not zero")
    }

    fun getFace(location: Location, block: Location): Vector {
        val directionBetween = between(block, location).normalize()
        val (x,y,z) = directionBetween.destructible
        val xAbs = abs(directionBetween.x)
        val yAbs = abs(directionBetween.y)
        val zAbs = abs(directionBetween.z)
        val max = max(max(xAbs, yAbs), zAbs)
        return when {
            max == xAbs && x < 0.5 -> Vector(-1.0, 0.0, 0.0)
            max == xAbs && x >= 0.5 -> Vector(1.0, 0.0, 0.0)
            max == yAbs && y < 0.5 -> Vector(0.0, -1.0, 0.0)
            max == yAbs && y >= 0.5 -> Vector(0.0, 1.0, 0.0)
            max == zAbs && z < 0.5 -> Vector(0.0, 0.0, -1.0)
            max == zAbs && z >= 0.5 -> Vector(0.0, 0.0, 1.0)
            else -> Vector(0.0, 1.0, 0.0)
        }
    }

    fun zero() = Vector(0.0,0.0,0.0)

    fun random() = Vector(
        Random.nextDouble(-1.0,1.0),
        Random.nextDouble(-1.0,1.0),
        Random.nextDouble(-1.0,1.0)
    ).normalize()

    fun Location.toGlobal(anchor: Location): Location {
        val yawAngle = anchor.yaw.toDouble()
        val offset = anchor.toVector()
        val rotated = this.toVector().rotateAroundY(Math.toRadians(-yawAngle))
        rotated.add(offset)
        return rotated.toLocation(anchor.world!!, anchor.yaw + yaw, anchor.pitch + pitch)
    }
}