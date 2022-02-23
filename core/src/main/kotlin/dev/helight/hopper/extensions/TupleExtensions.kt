package dev.helight.hopper.extensions

import dev.helight.hopper.utilities.kvec.Vector3
import org.bukkit.util.Vector

object TupleExtensions {

}

object FloatTripleExtensions {
    val Triple<Float, Float, Float>.vec3: Vector3
        get() = Vector3(first.toDouble(), second.toDouble(), third.toDouble())

    val Triple<Float, Float, Float>.vector : Vector
        get() = Vector(first, second, third)
}

object DoubleTripleExtensions {
    val Triple<Double, Double, Double>.vec3 : Vector3
        get() = Vector3(first, second, third)

    val Triple<Double, Double, Double>.vector : Vector
        get() = Vector(first, second, third)
}