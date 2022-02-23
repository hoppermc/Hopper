package dev.helight.hopper.ecs.craft.listeners

import dev.helight.hopper.TransientEntity
import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.craft.EcsPlayer
import dev.helight.hopper.ecs.impl.components.HopperHealth
import dev.helight.hopper.ecs.impl.components.HopperRegen
import dev.helight.hopper.ecs.impl.events.HopperPlayerJoinEvent
import dev.helight.hopper.ecs.impl.events.HopperPlayerQuitEvent
import dev.helight.hopper.offstageAsync
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerEngineListener : BetterListener() {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        ecs.create {
            tag<TransientEntity>()
            add<EcsPlayer>(EcsPlayer.fromPlayer(event.player))
            add<HopperHealth>(HopperHealth(20.0, 20.0))
            add<HopperRegen>(HopperRegen.create(1.0))
            ecs.eventWithCallback(HopperPlayerJoinEvent(this, event.player)).await()
        }
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        val exported = EcsPlayer.of(event.player)
        offstageAsync {
            ecs.eventWithCallback(HopperPlayerQuitEvent(exported, event.player)).await()
            exported.delete()
        }
    }

}