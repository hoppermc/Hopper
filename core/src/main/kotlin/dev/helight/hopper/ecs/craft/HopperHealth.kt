package dev.helight.hopper.ecs.craft

import dev.helight.hopper.*
import dev.helight.hopper.data.KeyLocks
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.event.HopperEventHandler
import dev.helight.hopper.ecs.impl.components.*
import dev.helight.hopper.ecs.impl.events.*
import dev.helight.hopper.extensions.FloatTripleExtensions.vector
import org.bukkit.EntityEffect
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

class HealthAmountTransformer(
    val amount: Double,
) {
    val delegate: ComponentTransformer = ::transform

    fun transform(a: EntityId, b: ComponentID, c: ComponentData): ComponentData {
        return (c as HopperHealth).copy(health = min(c.health + amount, c.maxHealth))
    }
}

class HealthSetAmountTransformer(
    val amount: Double,
) {
    val delegate: ComponentTransformer = ::transform

    fun transform(a: EntityId, b: ComponentID, c: ComponentData): ComponentData {
        return (c as HopperHealth).copy(health = min(amount, c.maxHealth))
    }
}

class SetMaxHealthTransformer(
    val maxHealth: Double,
) {
    val delegate: ComponentTransformer = ::transform

    fun transform(a: EntityId, b: ComponentID, c: ComponentData): ComponentData {
        return (c as HopperHealth).copy(maxHealth = maxHealth)
    }
}

class HealthDamageTransformer(
    val damage: HopperDamage,
) {
    val delegate: ComponentTransformer = ::transform

    fun transform(a: EntityId, b: ComponentID, c: ComponentData): ComponentData {
        return (c as HopperHealth).copy(health = min(c.health - damage.damage, c.maxHealth), lastDamage = damage) // Just to be sure, use max
    }
}

class SetRegenRateTransformer(
    val rate: Double,
) {
    val delegate: ComponentTransformer = ::transform

    fun transform(a: EntityId, b: ComponentID, c: ComponentData): ComponentData {
        return (c as HopperRegen).copy(rate = rate)
    }
}

class IncreaseMaxHealthReevaluateHandler : HopperEventHandler(MaxHealthReevaluateEvent::class.java) {
    override suspend fun handle(event: ExportedEntityWrapper) {
        if (hopper.isShutdown) return
        val maxHealthReevaluateEvent = event.get<MaxHealthReevaluateEvent>()
        val spigotPlayer = maxHealthReevaluateEvent.entity.get<EcsPlayer>()
        val player = spigotPlayer.get()!!
        val items = EcsItem.allInPlayer(player)

        items.filter {
            it.has<IncreaseMaxHealth>()
        }.forEach {
            val increaseMaxHealth = it.get<IncreaseMaxHealth>()
            if (it.has<ActiveWearable>()) {
                if (EcsItem.isWearing(it.entityId, player)) maxHealthReevaluateEvent.health += increaseMaxHealth.increase
            } else {
                maxHealthReevaluateEvent.health += increaseMaxHealth.increase
            }
        }
    }
}

class IncreaseRegenReevaluateHandler : HopperEventHandler(RegenReevaluateEvent::class.java) {
    override suspend fun handle(event: ExportedEntityWrapper) {
        if (hopper.isShutdown) return
        val regenReevaluateEvent = event.get<RegenReevaluateEvent>()
        val spigotPlayer = regenReevaluateEvent.entity.get<EcsPlayer>()
        val player = spigotPlayer.get()!!
        val items = EcsItem.allInPlayer(player)

        items.filter {
            it.has<IncreaseRegenRate>()
        }.forEach {
            val increaseRegen = it.get<IncreaseRegenRate>()
            if (it.has<ActiveWearable>()) {
                if (EcsItem.isWearing(it.entityId, player)) regenReevaluateEvent.rate += increaseRegen.increase
            } else {
                regenReevaluateEvent.rate += increaseRegen.increase
            }
        }
    }
}

class ExtendHealthItemInfo : HopperEventHandler(CollectDetailedItemInfos::class.java) {
    override suspend fun handle(event: ExportedEntityWrapper) {
        val itemInfos = event.get<CollectDetailedItemInfos>()
        val player = itemInfos.player
        val item = itemInfos.entity

        if (item.has<IncreaseMaxHealth>()) {
            itemInfos.lore.add("§7Maximale Leben: §7${item.get<IncreaseMaxHealth>().increase}")
        }

        if (item.has<IncreaseRegenRate>()) {
            itemInfos.lore.add("§7Regeneration: §7${item.get<IncreaseRegenRate>().increase}")
        }
    }
}

object HopperHealthUtils {
    val keyedLocks = KeyLocks<EntityId>()

    fun invokeDamage(entityId: EntityId, damage: HopperDamage) = offstageAsync {
        if (hopper.isShutdown) return@offstageAsync
        keyedLocks.lock(entityId)
        try {
            val entity = ecs.get(entityId) ?: run {
                println(ecs.storage.globalEntityList)
                error("Error resolving entity $entityId")
            }
            val originHealth = entity.get<HopperHealth>()

            if (damage.timestamp.minus(originHealth.lastDamage.timestamp) <= 50.milliseconds &&
                    damage.damageType == originHealth.lastDamage.damageType) {
                println("Skipping duplicated attack")
                return@offstageAsync
            }

            println("Damage Event Start [${originHealth.health}] isAttackedByHopper=${damage.source != null}")
            val result = ecs.eventWithCallback(HopperDamageEvent(entity, damage)).await()
            val resultDamage = result.get<HopperDamageEvent>().damage
            if (entity.has<EcsPlayer>()) {
                val player = entity.get<EcsPlayer>().get()!!
                player.velocity = player.velocity.add(resultDamage.knockback.vector)
                player.playEffect(EntityEffect.HURT)
            } else if (entity.has<EcsMob>()) {
                val mob = entity.get<EcsMob>().resolve()!!
                mob.velocity = mob.velocity.add(resultDamage.knockback.vector)
                mob.playEffect(EntityEffect.HURT)
            }
            ecs.transform<HopperHealth>(entityId, HealthDamageTransformer(resultDamage)::transform)
        } finally {
            keyedLocks.unlock(entityId)
        }
    }

    fun invokeHeal(entityId: EntityId, amount: Double, reason: String) = offstageAsync {
        var entity = ecs.get(entityId)!!
        println("Heal Event Start [${entity.get<HopperHealth>().health}]")
        val result = ecs.eventWithCallback(HopperHealEvent(entity, amount, reason)).await()
        val heal = result.get<HopperHealEvent>()
        ecs.transform<HopperHealth>(entityId, HealthAmountTransformer(heal.amount)::transform)
    }
}