package dev.helight.hopper.ecs.event

import dev.helight.hopper.registry.Registrable
import java.util.*

abstract class DirectEventHandle<E: DirectEvent>(
    val priority: Int = 0,
    val clazz: Class<E>
) : Registrable {
    abstract fun handle(event: E)
    override fun registeredId(): String = clazz.name
    override fun registeredUuid(): UUID = UUID.randomUUID()
}