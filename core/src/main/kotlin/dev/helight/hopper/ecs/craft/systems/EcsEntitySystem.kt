package dev.helight.hopper.ecs.craft.systems

import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.craft.EcsMob
import dev.helight.hopper.ecs.system.HopperSystem
import dev.helight.hopper.ecs.system.HopperSystemOptions
import dev.helight.hopper.toKey
import kotlinx.coroutines.runBlocking

@ExperimentalUnsignedTypes
class EcsEntitySystem : HopperSystem(sortedSetOf(EcsMob::class.java.toKey()),
    HopperSystemOptions(isTicking = true, expanded = true)) {

    override fun tickIndividual(wrapper: ExportedEntityWrapper) = runBlocking {
        val entityComponent = wrapper.get<EcsMob>()
        if (entityComponent.isCurrentlyLoaded()) {
            //println("System ticking for ${entityComponent.entityId}")
        } else {
            println("Entity ${entityComponent.entityId} is not loaded")
        }
    }
}