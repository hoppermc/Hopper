package dev.helight.hopper.entity

import dev.helight.hopper.api.BetterListener
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

class EntityEngineListener : BetterListener() {

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        if (!event.isNewChunk) {
            for (entity in event.chunk.entities) {
                SpigotEntity.invalidateEntity(entity)

                val hopperID = SpigotEntity.getHopper(entity)
                if (hopperID != null) {
                    SpigotEntity.load(entity)
                }
            }
        }
    }

    @EventHandler
    fun onChunkUnload(event: ChunkUnloadEvent) {
        for (entity in event.chunk.entities) {
            SpigotEntity.invalidateEntity(entity)

            val hopperID = SpigotEntity.getHopper(entity)
            if (hopperID != null) {
                SpigotEntity.store(entity, hopperID)
            }
        }
    }

    /*

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        val actor = Actors.actorRegistry.findByUuid(event.entity.uniqueId)
        if (actor != null) {
            val blueprint = actor.blueprint
            blueprint.eventHandler.onDamage(actor, event)
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val actor = Actors.actorRegistry.findByUuid(event.entity.uniqueId)
        if (actor != null) {
            val blueprint = actor.blueprint
            blueprint.eventHandler.onDamageByEntity(actor, event)
        }
    }
    */

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        SpigotEntity.invalidateEntity(event.entity.uniqueId.toString())
        SpigotEntity.delete(event.entity.uniqueId.toString())
    }

}