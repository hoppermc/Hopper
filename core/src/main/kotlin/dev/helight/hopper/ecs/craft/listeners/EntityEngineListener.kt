package dev.helight.hopper.ecs.craft.listeners

import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.ecs.craft.EcsMob
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

class EntityEngineListener : BetterListener() {

    @ExperimentalUnsignedTypes
    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        // This still doesn't work in 1.17+ why the f*ck isn't this getting fixed @md5 :3
        /*
        if (!event.isNewChunk) {
            val entities = event.chunk.entities
            entities.forEach { entity ->
                EcsMob.invalidateEntity(entity)
                val hopperID = EcsMob.getHopper(entity)
                if (hopperID != null) {
                    println("Confirmed as hopper entity. Loading.")
                    EcsMob.load(entity)
                }
            }
        }
        */
    }

    @EventHandler
    fun onChunkUnload(event: ChunkUnloadEvent) {

        for (entity in event.chunk.entities) {
            EcsMob.invalidateEntity(entity)

            val hopperID = EcsMob.getHopper(entity)
            if (hopperID != null) {
                println("Storing hopper entity mob")
                EcsMob.store(entity, hopperID)
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
        EcsMob.invalidateEntity(event.entity.uniqueId.toString())
        EcsMob.delete(event.entity.uniqueId.toString())
    }

}