package dev.helight.hopper.effects

import de.slikey.effectlib.Effect
import de.slikey.effectlib.EffectManager
import de.slikey.effectlib.EffectType
import dev.helight.hopper.utilities.Chat
import dev.helight.hopper.utilities.MathUtils
import dev.helight.hopper.utilities.VectorUtils.between
import dev.helight.hopper.utilities.VectorUtils.maxVectorDistance
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle

class CuboidSelectionEffect(effectManager: EffectManager) : Effect(effectManager) {
    var actionbar = ""
    var wasLastLooking = false

    override fun onRun() {
        val world = location.world
        val ax = Math.min(location.x, getTarget().x)
        val ay = Math.min(location.y, getTarget().y)
        val az = Math.min(location.z, getTarget().z)
        val bx = Math.max(location.x, getTarget().x)
        val by = Math.max(location.y, getTarget().y)
        val bz = Math.max(location.z, getTarget().z)
        var x = ax
        while (x < bx) {
            display(Particle.REDSTONE, Location(world, x, ay, az), color, 0f, 1)
            display(Particle.REDSTONE, Location(world, x, by, az), color, 0f, 1)
            display(Particle.REDSTONE, Location(world, x, ay, bz), color, 0f, 1)
            display(Particle.REDSTONE, Location(world, x, by, bz), color, 0f, 1)
            x += 0.25
        }
        var y = ay
        while (y < by) {
            display(Particle.REDSTONE, Location(world, ax, y, az), color, 0f, 1)
            display(Particle.REDSTONE, Location(world, ax, y, bz), color, 0f, 1)
            display(Particle.REDSTONE, Location(world, bx, y, az), color, 0f, 1)
            display(Particle.REDSTONE, Location(world, bx, y, bz), color, 0f, 1)
            y += 0.25
        }
        var z = az
        while (z < bz) {
            display(Particle.REDSTONE, Location(world, ax, ay, z), color, 0f, 1)
            display(Particle.REDSTONE, Location(world, ax, by, z), color, 0f, 1)
            display(Particle.REDSTONE, Location(world, bx, ay, z), color, 0f, 1)
            display(Particle.REDSTONE, Location(world, bx, by, z), color, 0f, 1)
            z += 0.25
        }
        val a = Location(world, ax, ay, az).subtract(3.0, 3.0, 3.0)
        val b = Location(world, bx, by, bz).add(3.0, 3.0, 3.0)
        val center = b.clone().subtract(a.clone()).multiply(0.5).add(a.clone())
        if (MathUtils.containsBox(a, b, targetPlayer.location)) {
            val dist: Double = maxVectorDistance(
                targetPlayer.eyeLocation.direction,
                between(targetPlayer.eyeLocation, center)
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