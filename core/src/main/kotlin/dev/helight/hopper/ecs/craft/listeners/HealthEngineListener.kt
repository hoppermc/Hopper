package dev.helight.hopper.ecs.craft.listeners

import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.ecs.craft.EcsMob
import dev.helight.hopper.ecs.craft.EcsPlayer
import dev.helight.hopper.ecs.craft.HopperHealthUtils
import dev.helight.hopper.ecs.impl.components.HopperDamage
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent

class HealthEngineListener : BetterListener() {

    @Suppress("DuplicatedCode")
    @EventHandler
    fun entityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        val hopper = EcsMob.getHopper(event.entity)
        val attackerHopper = EcsMob.getHopper(event.damager)
        when {
            event.entity is Player && event.damager !is Player -> {
                println("Player <- !Player")
                event.isCancelled = true
                val entity = EcsPlayer.of(event.entity as Player)
                val damage: HopperDamage = if (attackerHopper == null) {
                    HopperDamage.eveAttack(event.entity, event.damager, event.damage,
                        damageType = EntityDamageEvent.DamageCause.ENTITY_ATTACK.ordinal,
                        sourceType = event.damager.type.ordinal
                    )
                } else {
                    HopperDamage.eveAttack(event.entity, event.damager, event.damage,
                        damageType = EntityDamageEvent.DamageCause.ENTITY_ATTACK.ordinal,
                        sourceType = event.damager.type.ordinal, source = attackerHopper
                    )
                }
                HopperHealthUtils.invokeDamage(entity.entityId, damage)
            }
            event.entity is Player && event.damager is Player -> {
                println("Player <- Player")
                event.isCancelled = true
                val entity = EcsPlayer.of(event.entity as Player)
                val attacker = EcsPlayer.of(event.damager as Player)
                val damage = HopperDamage.eveAttack(event.entity, event.damager, event.damage,
                    damageType = EntityDamageEvent.DamageCause.ENTITY_ATTACK.ordinal,
                    sourceType = event.damager.type.ordinal, source = attacker.entityId
                )
                HopperHealthUtils.invokeDamage(entity.entityId, damage)
            }
            event.entity !is Player && hopper != null && event.damager !is Player -> {
                println("!Player <- !Player")
                event.isCancelled = true
                val damage: HopperDamage = if (attackerHopper == null) {
                    HopperDamage.eveAttack(event.entity, event.damager, event.damage,
                        damageType = EntityDamageEvent.DamageCause.ENTITY_ATTACK.ordinal,
                        sourceType = event.damager.type.ordinal
                    )
                } else {
                    HopperDamage.eveAttack(event.entity, event.damager, event.damage,
                        damageType = EntityDamageEvent.DamageCause.ENTITY_ATTACK.ordinal,
                        sourceType = event.damager.type.ordinal, source = attackerHopper
                    )
                }
                HopperHealthUtils.invokeDamage(hopper, damage)
            }
            event.entity !is Player && hopper != null && event.damager is Player -> {
                println("!Player <- Player")
                event.isCancelled = true
                val attacker = EcsPlayer.of(event.damager as Player)
                val damage = HopperDamage.eveAttack(event.entity, event.damager, event.damage,
                    damageType = EntityDamageEvent.DamageCause.ENTITY_ATTACK.ordinal,
                    sourceType = event.damager.type.ordinal, source = attacker.entityId
                )
                HopperHealthUtils.invokeDamage(hopper, damage)
            }
        }
    }

    @EventHandler
    fun heal(event: EntityRegainHealthEvent) {
        if (event.entity is Player) {
            event.isCancelled = true
            if (event.regainReason == EntityRegainHealthEvent.RegainReason.REGEN) return
            val entity = EcsPlayer.of(event.entity as Player)
            HopperHealthUtils.invokeHeal(entity.entityId, event.amount, event.regainReason.name)
        }
    }

    @EventHandler
    fun damageEvent(event: EntityDamageEvent) {
        if (event.isCancelled) return
        if (event.entity is Player) {
            event.isCancelled = true
            val damage = HopperDamage.instantAttack(event.damage, damageType = event.cause.ordinal)
            val entity = EcsPlayer.of(event.entity as Player)
            HopperHealthUtils.invokeDamage(entity.entityId, damage)
        } else {
            val hopper = EcsMob.getHopper(event.entity)
            if (hopper != null) {
                event.isCancelled = true
                val damage = HopperDamage.instantAttack(event.damage, damageType = event.cause.ordinal)
                println("Invoking damage for non player entity")
                HopperHealthUtils.invokeDamage(hopper, damage)
            }
        }
    }
}