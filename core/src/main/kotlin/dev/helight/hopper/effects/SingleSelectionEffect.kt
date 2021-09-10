package dev.helight.hopper.effects

import de.slikey.effectlib.Effect
import de.slikey.effectlib.EffectManager
import de.slikey.effectlib.EffectType
import dev.helight.hopper.utilities.Chat
import dev.helight.hopper.utilities.MathUtils
import dev.helight.hopper.utilities.VectorUtils
import org.bukkit.Color
import org.bukkit.Particle

class SingleSelectionEffect(effectManager: EffectManager) : Effect(effectManager) {
    var actionbar = ""
    var wasLastLooking = false

    override fun onRun() {
        display(Particle.REDSTONE, location, color)
        val a = location.clone().subtract(3.0, 3.0, 3.0)
        val b = location.clone().add(3.0, 3.0, 3.0)
        if (MathUtils.containsBox(a, b, targetPlayer.location)) {
            val dist: Double = VectorUtils.maxVectorDistance(
                targetPlayer.eyeLocation.direction,
                VectorUtils.between(targetPlayer.eyeLocation, location)
            )
            if (dist <= 0.5) {
                Chat.sendActionbar(targetPlayer, actionbar)
                wasLastLooking = true
            } else if (wasLastLooking) {
                wasLastLooking = false
                Chat.sendActionbar(targetPlayer, "")
            }
        } else if (wasLastLooking) {
            wasLastLooking = false
            Chat.sendActionbar(targetPlayer, "")
        }
    }

    init {
        type = EffectType.REPEATING
        period = 5
        iterations = -1
        color = Color.SILVER
    }
}