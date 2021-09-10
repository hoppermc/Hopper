package dev.helight.hopper.extensions

import dev.helight.hopper.utilities.kvec.Matrix4x4
import dev.helight.hopper.utilities.kvec.Vector3
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.util.Vector

object VLBExtensions {

    val Block.center
        get() = this.location.add(0.5, 0.5, 0.5)

    val Vector.destructible
        get() = DestructibleVector(this.x, this.y, this.z)

    val Vector.vec3
        get() = Vector3(x, y, z)

    val Vector.matrix
        get() = Matrix4x4.fromLocation(Location(null, x, y, z,0f,0f))

    val Block.destructible
        get() = location.destructible

    val Location.destructible
        get() = toVector().destructible

    val Location.matrix
        get() = Matrix4x4.fromLocation(this)

    data class DestructibleVector(
        val x: Double,
        val y: Double,
        val z: Double
    )
}