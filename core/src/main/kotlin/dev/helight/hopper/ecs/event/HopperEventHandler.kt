package dev.helight.hopper.ecs.event

import dev.helight.hopper.ComponentGroup
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.registry.SimpleRegistrable
import dev.helight.hopper.toKey
import java.util.*
import kotlin.reflect.KClass

@ExperimentalUnsignedTypes
abstract class HopperEventHandler(
    val componentGroup: ComponentGroup,
    val priority: Int = 0
) : SimpleRegistrable {

    constructor(vararg classes: Class<*>) : this(TreeSet(classes.map { it.toKey() }), 0)
    constructor(vararg classes: KClass<*>) : this(TreeSet(classes.map { it.java.toKey() }), 0)
    constructor(priority: Int, vararg classes: Class<*>) : this(TreeSet(classes.map { it.toKey() }), priority)
    constructor(priority: Int, vararg classes: KClass<*>) : this(TreeSet(classes.map { it.java.toKey() }), priority)

    abstract suspend fun handle(event: ExportedEntityWrapper)
    override fun registeredId(): String = javaClass.name
}
