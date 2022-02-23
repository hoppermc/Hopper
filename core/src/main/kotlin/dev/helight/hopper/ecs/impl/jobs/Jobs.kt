package dev.helight.hopper.ecs.impl.jobs

import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.craft.EcsItem
import dev.helight.hopper.ecs.craft.EcsPlayer
import dev.helight.hopper.ecs.craft.SetMaxHealthTransformer
import dev.helight.hopper.ecs.craft.SetRegenRateTransformer
import dev.helight.hopper.ecs.impl.components.HopperHealth
import dev.helight.hopper.ecs.impl.components.HopperRegen
import dev.helight.hopper.ecs.impl.events.MaxHealthReevaluateEvent
import dev.helight.hopper.ecs.impl.events.RegenReevaluateEvent
import dev.helight.hopper.offstageAsync
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.quartz.Job
import org.quartz.JobExecutionContext

@Suppress("EXPERIMENTAL_API_USAGE")
class ItemJob : Job {

    @Suppress("UNREACHABLE_CODE")
    override fun execute(context: JobExecutionContext?) {
        Bukkit.getOnlinePlayers().forEach { player ->
            player.inventory.filterNotNull().forEach {
                val hopper = EcsItem.getHopper(it)
                if (hopper != null && !ecs.storage.containsEntity(hopper)) {
                    offstageAsync {
                        println("SpigotItem $hopper is not in system, trying to load it")
                        EcsItem.load(it, player)
                    }
                } else if (hopper != null ){
                    offstageAsync {
                        EcsItem.peekStore(it, hopper)
                    }
                }
            }
        }

        ecs.query(EcsItem::class.java).filter {
            val spigotItem = it.get<EcsItem>()
            val holder = spigotItem.getHolder() ?: return@filter true
            return@filter !holder.inventory.any { item ->
                if (item == null) return@any false
                val hopper = EcsItem.getHopper(item) ?: return@any false
                hopper == it.entityId
            }
        }.forEach {
            println("ECS Item not present in player anymore")
            ecs.deleteEntity(it.entityId)
        }
    }

    companion object {
        fun quickCheckPlayer(player: Player) {
            player.inventory.filterNotNull().forEach {
                val hopper = EcsItem.getHopper(it)
                if (hopper != null && !ecs.storage.containsEntity(hopper)) {
                    offstageAsync {
                        println("SpigotItem $hopper is not in system, trying to load it")
                        EcsItem.load(it, player)
                    }
                }
            }

            val uid = player.uniqueId.toString()
            ecs.query(EcsItem::class.java)
                .filter {
                    it.get<EcsItem>().holder == uid
                }.filter {
                    val spigotItem = it.get<EcsItem>()
                    val holder = spigotItem.getHolder() ?: return@filter true
                    return@filter !holder.inventory.any { item ->
                        if (item == null) return@any false
                        val hopper = EcsItem.getHopper(item) ?: return@any false
                        hopper == it.entityId
                    }
                }.forEach {
                    println("ECS Item not present in player anymore")
                    ecs.deleteEntity(it.entityId)
                }
        }
    }
}

class MaxHealthReevaluationJob : Job {
    override fun execute(context: JobExecutionContext) = runBlocking {
        EcsPlayer.all().forEach {
            val health = it.get<HopperHealth>()
            val event = MaxHealthReevaluateEvent(it, health.baseMaxHealth)
            ecs.eventWithCallback(event).await()
            ecs.transform<HopperHealth>(it.entityId, SetMaxHealthTransformer(event.health)::transform)
        }
    }
}

class RegenReevaluationJob : Job {
    override fun execute(context: JobExecutionContext) = runBlocking {
        EcsPlayer.all().forEach {
            val regen = it.get<HopperRegen>()
            val event = RegenReevaluateEvent(it, regen.baseRate)
            ecs.eventWithCallback(event).await()
            ecs.transform<HopperRegen>(it.entityId, SetRegenRateTransformer(event.rate)::transform)
        }
    }
}

class LoadedEntityCacheJob : Job {

    override fun execute(context: JobExecutionContext?) {
        entityIds = Bukkit.getWorlds().flatMap { world ->
            world.entities.map{ it.uniqueId.toString() }
        }
    }

    companion object {
        var entityIds: List<String> = listOf()
    }
}