package dev.helight.hopper.ecs.impl.events

import dev.helight.hopper.EntityId
import dev.helight.hopper.ecs.BufferedEntity
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.craft.EcsMob
import dev.helight.hopper.ecs.event.DirectEvent
import dev.helight.hopper.ecs.event.Event
import dev.helight.hopper.ecs.impl.components.HopperDamage
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class HopperEntityCreateEvent(val delegate: Entity, val hopperEntity: EcsMob) : EntityEvent(delegate) {
    override fun getHandlers(): HandlerList = handlers

    companion object {
        val handlers = HandlerList()
    }
}

class HopperEntitySpawnEvent(val delegate: Entity, val hopperEntity: EcsMob) : EntityEvent(delegate) {
    override fun getHandlers(): HandlerList = handlers

    companion object {
        val handlers = HandlerList()
    }
}

class HopperEntityDespawnEvent(val delegate: Entity, val hopperEntity: EcsMob) : EntityEvent(delegate) {
    override fun getHandlers(): HandlerList = handlers

    companion object {
        val handlers = HandlerList()

    }
}

class HopperEntityDestroyEvent(val delegate: Entity, val hopperEntity: EcsMob) : EntityEvent(delegate) {
    override fun getHandlers(): HandlerList = handlers

    companion object {
        val handlers = HandlerList()
    }
}

data class HopperDamageEvent(
    val receiver: ExportedEntityWrapper,
    var damage: HopperDamage
) : Event

data class HopperHealEvent(
    val receiver: ExportedEntityWrapper,
    var amount: Double,
    val reason: String
) : Event

data class MaxHealthReevaluateEvent(
    val entity: ExportedEntityWrapper,
    var health: Double
) : Event

data class RegenReevaluateEvent(
    val entity: ExportedEntityWrapper,
    var rate: Double
) : Event

data class ItemPickupEvent(
    val entityId: EntityId
) : Event

data class ItemDropEvent(
    val entityId: EntityId
) : Event

@ExperimentalUnsignedTypes
data class ItemLoadEvent(
    val entityId: EntityId,
    val data: ExportedEntityWrapper
) : Event

data class ItemStoreEvent(
    val entityId: EntityId
) : Event

data class ItemInteractEvent(
    val entity: ExportedEntityWrapper,
    val delegate: PlayerInteractEvent
) : DirectEvent

data class HopperPlayerJoinEvent(
    val buffer: BufferedEntity,
    val player: Player
) : Event

data class HopperPlayerQuitEvent(
    val buffer: ExportedEntityWrapper,
    val player: Player
) : Event

data class CollectDetailedItemInfos(
    val entity: ExportedEntityWrapper,
    val player: Player,
    val lore: MutableList<String> = mutableListOf()
) : Event