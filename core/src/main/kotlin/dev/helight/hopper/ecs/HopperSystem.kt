package dev.helight.hopper.ecs

import dev.helight.hopper.ComponentGroup
import dev.helight.hopper.registry.SimpleRegistrable

@ExperimentalUnsignedTypes
abstract class HopperSystem(
    val componentGroup: ComponentGroup,
    val options: HopperSystemOptions
) : SimpleRegistrable {

    open fun start() {}
    open fun stop() {}
    open fun tick(entities: List<ExportedEntityWrapper>) {
        entities.forEach(this::tickIndividual)
    }
    abstract fun tickIndividual(entity: ExportedEntityWrapper)

    open fun create(entity: ExportedEntityWrapper) {}
    open fun resume(entity: ExportedEntityWrapper) {}
    open fun halt(entity: ExportedEntityWrapper) {}

    override fun registeredId(): String = javaClass.name
}

data class HopperSystemOptions(
    val isTicking: Boolean = true
)