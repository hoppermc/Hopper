package dev.helight.hopper.ecs.impl

import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.event.HopperEventHandler
import dev.helight.hopper.ecs.impl.components.HopperEvent

@ExperimentalUnsignedTypes
class DebugEventHandler : HopperEventHandler(HopperEvent::class) {
    override suspend fun handle(event: ExportedEntityWrapper) {
        //println("Event '${event.data.first { it.second is Event }.second?.javaClass?.simpleName ?: "null"}' [${event.entityId}]")
    }

}