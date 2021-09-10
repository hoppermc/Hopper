package dev.helight.hopper.effects

import de.slikey.effectlib.Effect
import de.slikey.effectlib.EffectType
import de.slikey.effectlib.effect.LineEffect
import dev.helight.hopper.decouple
import dev.helight.hopper.hopper
import dev.helight.hopper.sleep
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import java.time.Duration

object Effects {

    fun cuboidSelection(a: Location, b: Location, player: Player, actionbar: String = "", color: Color = Color.WHITE): CuboidSelectionEffect {
        val effect = CuboidSelectionEffect(hopper.spigot.effectManager)
        effect.location = a
        effect.color = color
        effect.setTargetLocation(b)
        effect.setTargetPlayer(player)
        effect.start()
        effect.actionbar = actionbar
        return effect
    }

    fun singleSelection(a: Location, player: Player, actionbar: String = "", color: Color = Color.WHITE): SingleSelectionEffect {
        val effect = SingleSelectionEffect(hopper.spigot.effectManager)
        effect.location = a
        effect.color = color
        effect.setTargetPlayer(player)
        effect.actionbar = actionbar
        effect.start()
        return effect
    }

    fun display(a: Location, color: Color = Color.WHITE, particleType: Particle = Particle.REDSTONE) {
        hopper.spigot.effectManager.display(
            particleType,
            a,
            0f,
            0f,
            0f,
            0f,
            1,
            1f,
            color,
            null,
            0,
            200.0,
            Bukkit.getOnlinePlayers().toList()
        )
    }

    fun line(a: Location, b: Location, player: Player, color: Color = Color.WHITE) : LineEffect {
        val effect = LineEffect(hopper.spigot.effectManager)
        effect.type = EffectType.REPEATING
        effect.period = 5
        effect.iterations = -1
        effect.location = a
        effect.color = color
        effect.particle = Particle.REDSTONE
        effect.setTargetLocation(b)
        effect.targetPlayer = player
        effect.start()
        return effect
    }

    fun Effect.cancelAfter(duration: Duration) = decouple {
        sleep(duration)
        this.cancel()
    }

}