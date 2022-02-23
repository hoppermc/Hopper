package dev.helight.hopper.ecs.craft.systems

import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.craft.EcsMob
import dev.helight.hopper.ecs.impl.components.HopperHealth
import dev.helight.hopper.ecs.system.HopperSystem
import dev.helight.hopper.extensions.EntityExtensions.living
import dev.helight.hopper.synchronizeDecoupled

class HopperHealthMobSystem : HopperSystem(EcsMob::class.java, HopperHealth::class.java) {
    override fun tickIndividual(entity: ExportedEntityWrapper) {
        val mob = entity.get<EcsMob>()
        val health = entity.get<HopperHealth>()

        if (health.isDead) {
            synchronizeDecoupled {
                val resolved = mob.resolve()!!
                val living = resolved.living!!
                living.health = 0.0
            }
            //TODO: Death Event
            println("Mob ${mob.entityId} is dead")
            entity.delete()
        } else {
            //println("Mob ${mob.entityId} has $health")
        }
    }

}