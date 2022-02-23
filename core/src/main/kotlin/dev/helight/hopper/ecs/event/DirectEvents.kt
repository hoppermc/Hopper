package dev.helight.hopper.ecs.event

import dev.helight.hopper.registry.Registry

class DirectEvents {
    val handlers = Registry<DirectEventHandle<*>>()

    fun <E: DirectEvent> invoke(event: E) {
        handlers.findById(event::class.java.name).sortedByDescending {
            it.priority
        }.forEach {
            val handler = it as DirectEventHandle<E>
            handler.handle(event)
        }
    }

    fun register(handler: DirectEventHandle<*>) {
        handlers.register(handler)
    }

    inline fun <reified E: DirectEvent, reified H: DirectEventHandle<E>> assureRegistered() {
        val exists = handlers.findById(E::class.java.name).any { it::class.java.isAssignableFrom(H::class.java) }
        if (!exists) register(H::class.java.newInstance())
    }

}