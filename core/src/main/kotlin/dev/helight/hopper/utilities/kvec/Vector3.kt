package dev.helight.hopper.utilities.kvec

import org.bukkit.util.Vector
import kotlin.math.round
import kotlin.math.sqrt

class Vector3 {

    var x: Double
    var y: Double
    var z: Double

    constructor() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    constructor(vector: Vector) : this(vector.x, vector.y, vector.z) {}

    constructor(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    val xY: Vector2
        get() = Vector2(x, y)
    val xZ: Vector2
        get() = Vector2(x, z)
    val bukkit: Vector
        get() = Vector(x, y, z)
    val doubleTriple: Triple<Double, Double, Double>
        get() = Triple(x, y, z)
    val floatTriple: Triple<Float, Float, Float>
        get() = Triple(x.toFloat(), y.toFloat(), z.toFloat())

    /**
     * Returns a new vector with the x/y/z values inverted.
     *
     * @return negated vector
     */
    fun negate(): Vector3 {
        return Vector3(-x, -y, -z)
    }

    /**
     * Returns a new normalized vector of this vector (length = 1)
     *
     * @return normalized vector
     */
    fun normalize(): Vector3 {
        val len = sqrt(x * x + y * y + z * z)
        return Vector3(x / len, y / len, z / len)
    }

    fun distanceSquared(v: Vector3): Double {
        val dx = x - v.x
        val dy = y - v.y
        val dz = z - v.z
        return dx * dx + dy * dy + dz * dz
    }

    fun clone(): Vector3 {
        return Vector3(x, y, z)
    }

    operator fun plus(other: Vector3): Vector3 {
        return Vector3(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Vector3): Vector3 {
        return Vector3(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(other: Vector3): Vector3 {
        return Vector3(x * other.x, y * other.y, z * other.z)
    }

    operator fun times(scalar: Double): Vector3 {
        return Vector3(x * scalar, y * scalar, z * scalar)
    }

    operator fun div(other: Vector3): Vector3 {
        return Vector3(x / other.x, y / other.y, z / other.z)
    }

    operator fun div(scalar: Double): Vector3 {
        return Vector3(x / scalar, y / scalar, z / scalar)
    }

    override fun toString(): String {
        return "{x=$x, y=$y, z=$z}"
    }

    fun roundedString(): String {
        return  "{x=${round(x * 100) / 100}, y=${round(y * 100) / 100}, z=${round(z * 100) / 100}}"
    }

    fun equals(p: Vector3): Boolean {
        return p.x == x && p.y == y && p.z == z
    }

    companion object {
        /**
         * Returns the cross product of two vectors
         *
         * @param v1 first vector
         * @param v2 second vector
         * @return cross product
         */
        fun cross(v1: Vector3, v2: Vector3): Vector3 {
            return Vector3(
                v1.y * v2.z - v1.z * v2.y,
                v2.x * v1.z - v2.z * v1.x,
                v1.x * v2.y - v1.y * v2.x
            )
        }

        /**
         * Returns the vector subtraction of two vectors (v1 - v2)
         *
         * @param v1
         * @param v2
         * @return subtracted vector
         */
        fun subtract(v1: Vector3, v2: Vector3): Vector3 {
            return Vector3(
                v1.x - v2.x,
                v1.y - v2.y,
                v1.z - v2.z
            )
        }

        fun add(v1: Vector3, v2: Vector3): Vector3 {
            return Vector3(
                v1.x + v2.x,
                v1.y + v2.y,
                v1.z + v2.z
            )
        }

        /**
         * Returns the vector average of two vectors ((v1 + v2) / 2)
         *
         * @param v1
         * @param v2
         * @return average vector
         */
        fun average(v1: Vector3, v2: Vector3): Vector3 {
            return Vector3(
                (v1.x + v2.x) / 2.0f,
                (v1.y + v2.y) / 2.0f,
                (v1.z + v2.z) / 2.0f
            )
        }

        /**
         * Returns the vector dot product of two vectors (v1 . v2)
         *
         * @param v1
         * @param v2
         * @return vector dot product
         */
        fun dot(v1: Vector3, v2: Vector3): Double {
            return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
        }
    }
}