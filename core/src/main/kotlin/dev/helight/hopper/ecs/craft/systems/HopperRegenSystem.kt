package dev.helight.hopper.ecs.craft.systems

import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.craft.HealthAmountTransformer
import dev.helight.hopper.ecs.impl.components.HopperHealth
import dev.helight.hopper.ecs.impl.components.HopperRegen
import dev.helight.hopper.ecs.system.HopperSystem
import dev.helight.hopper.ecs.system.HopperSystemTicker
import dev.helight.hopper.hopper

class HopperRegenSystem : HopperSystem(HopperHealth::class.java, HopperRegen::class.java) {
    override fun tickIndividual(entity: ExportedEntityWrapper) {
        if (hopper.isShutdown) return
        val regen = entity.get<HopperRegen>()
        ecs.transform<HopperHealth>(entity.entityId, HealthAmountTransformer(regen.rate * HopperSystemTicker.deltaTime / HopperSystemTicker.tickRate)::transform)
    }
}