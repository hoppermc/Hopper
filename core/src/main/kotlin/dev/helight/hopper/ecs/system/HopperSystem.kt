package dev.helight.hopper.ecs.system

import dev.helight.hopper.ComponentGroup
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.registry.SimpleRegistrable
import dev.helight.hopper.toKey
import java.util.*
import kotlin.reflect.KClass

@ExperimentalUnsignedTypes
abstract class HopperSystem(
    val componentGroup: ComponentGroup,
    val options: HopperSystemOptions
) : SimpleRegistrable {

    constructor(vararg classes: Class<*>) : this(TreeSet(classes.map { it.toKey() }), HopperSystemOptions(true))
    constructor(vararg classes: KClass<*>) : this(TreeSet(classes.map { it.java.toKey() }), HopperSystemOptions(true))

    open fun start() {}
    open fun stop() {}

    open fun tick(entities: List<ExportedEntityWrapper>) {
        entities.forEach(this::tickIndividual)
    }
    abstract fun tickIndividual(entity: ExportedEntityWrapper)
    override fun registeredId(): String = javaClass.name
}

data class HopperSystemOptions(
    val isTicking: Boolean = true,
    val expanded: Boolean = false
)