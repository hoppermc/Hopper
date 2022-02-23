package dev.helight.hopper.ecs.craft.systems

import dev.helight.hopper.EntityId
import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.craft.EcsPlayer
import dev.helight.hopper.ecs.impl.components.HopperHealth
import dev.helight.hopper.ecs.system.HopperSystem
import dev.helight.hopper.extensions.MiscExtensions.toStrippedString
import dev.helight.hopper.synchronizeDecoupled
import dev.helight.hopper.utilities.Chat
import kotlin.math.max

class HopperHealthPlayerSystem : HopperSystem(EcsPlayer::class.java, HopperHealth::class.java) {

    override fun tickIndividual(entity: ExportedEntityWrapper) {
        val player = entity.get<EcsPlayer>()
        val health = entity.get<HopperHealth>()

        if (health.isDead) {
            println("Player ${player.uuid} is dead")
            respawn(entity.entityId, health, player)
        } else {
            synchronizeDecoupled {
                val percent = health.health / health.maxHealth
                val newHealth = max(0.1, percent * 20.0)
                val p = player.get()!!
                if (p.health != newHealth) p.health = newHealth
                Chat.sendActionbar(p, health.health.toStrippedString() + "/" + health.maxHealth.toString())
            }
        }
    }

    fun respawn(id: EntityId, health: HopperHealth, spigotPlayer: EcsPlayer) {
        ecs.set<HopperHealth>(id, health.copy(health = health.baseMaxHealth))
    }
}

