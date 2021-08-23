package dev.helight.hopper.utilities

import dev.helight.hopper.HopperSpigotHook
import dev.helight.hopper.launchRepeatingTaskThreaded
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.floor

object Countdown {

    fun countdownBar(format: String, duration: Int, precision: Int = 10, decimal: Boolean = true, vararg players: Player) {
        val barUid = UUID.randomUUID().toString()
        val barKey = NamespacedKey(HopperSpigotHook.plugin, barUid)
        val secondFraction = 1000 / precision.toLong()
        val maxSteps = duration * precision
        val bar = Bukkit.createBossBar(barKey, format.replace("$$", ""), BarColor.PINK, BarStyle.SOLID)
        var step = 0
        bar.progress = 1.0
        if (players.isNotEmpty()) players.forEach { bar.addPlayer(it) }
        launchRepeatingTaskThreaded(0, secondFraction) {
            if (players.isEmpty()) Bukkit.getOnlinePlayers().forEach { bar.addPlayer(it) }
            val percentage = 1.0 - (step.toDouble() / (maxSteps.toDouble() - 1.0))
            val timeLeft = percentage * duration
            val decimalSecDeg = when {
                timeLeft < 10 && decimal -> true
                else -> false
            }
            val timeFormatted = when (decimalSecDeg) {
                true -> (floor(timeLeft * 10) / 10).toString()
                false -> timeLeft.toInt().toString()
            }
            bar.setTitle(format.replace("$$", timeFormatted))
            bar.progress = percentage
            step += 1
            step < maxSteps
        }.join()
        bar.removeAll()
        Bukkit.removeBossBar(barKey)
    }

}