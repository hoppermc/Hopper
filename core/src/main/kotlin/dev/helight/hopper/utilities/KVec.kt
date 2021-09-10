package dev.helight.hopper.utilities

import dev.helight.hopper.extensions.VLBExtensions.matrix
import dev.helight.hopper.extensions.VLBExtensions.vec3
import dev.helight.hopper.utilities.VectorUtils.toGlobal
import org.bukkit.Location
import org.bukkit.util.Vector


// All KVec Classes are sourced from https://github.com/bergerhealer/BKCommonLib/blob/master/src/main/java/com/bergerkiller
object KVec {

    /**
     * Transforms [vector] from local space to world space
     *
     * If the transform doesn't require the transformation of pitch and roll
     * please consider using [transformFast] which is faster and more efficient
     */
    infix fun Location.transform(vector: Vector): Location {
        val matrix = this.matrix
        val point = vector.vec3
        matrix.transformPoint(point)
        return point.bukkit.toLocation(this.world!!)
    }

    /**
     * Transforms [vector] from local space to world space
     *
     * If the transform doesn't require the transformation of pitch and roll
     * please consider using [transformFast] which is faster and more efficient
     */
    infix fun Vector.transform(vector: Vector): Vector {
        val matrix = this.matrix
        val point = vector.vec3
        matrix.transformPoint(point)
        return point.bukkit
    }

    /**
     * Transforms [vector] from local space to world space while omitting pitch and roll rotation
     */
    infix fun Location.transformFast(vector: Vector): Location {
        return vector.toLocation(this.world!!).toGlobal(this)
    }

    /**
     * Transforms [location] from world space to local space
     */
    infix fun Location.inverseTransform(location: Location): Vector {
        val matrix = this.matrix
        val point = location.toVector().vec3
        matrix.inverseTransformPoint(point)
        return point.bukkit
    }

    /**
     * Transforms [vector] from world space to local space
     */
    infix fun Vector.inverseTransform(vector: Vector): Vector {
        val matrix = this.matrix
        val point = vector.vec3
        matrix.inverseTransformPoint(point)
        return point.bukkit
    }

    infix fun Location.translate(offset: Vector): Location {
        val matrix = this.matrix
        matrix.translate(offset)
        return matrix.toLocation(this.world)
    }

    infix fun Vector.translate(offset: Vector): Vector {
        val matrix = this.matrix
        matrix.translate(offset)
        return matrix.toVector()
    }

}

