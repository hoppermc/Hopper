package dev.helight.hopper.ecs.impl

import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.impl.components.DebugComponent
import dev.helight.hopper.ecs.system.HopperSystem
import dev.helight.hopper.ecs.system.HopperSystemOptions
import dev.helight.hopper.extensions.ComponentGroupExtensions.componentGroup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@ExperimentalUnsignedTypes
class DebugComponentSystem : HopperSystem(setOf(DebugComponent::class.java).componentGroup(),
    HopperSystemOptions(expanded = true)) {

    override fun tickIndividual(entity: ExportedEntityWrapper) {
        println(entity.toString())
        val debug = entity.get<DebugComponent>()
        println("Ticking '${debug.name}' [${entity.entityId}] ${Json.encodeToString(entity.snapshot())}")
    }

}