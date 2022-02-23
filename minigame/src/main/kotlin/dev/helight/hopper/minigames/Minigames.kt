package dev.helight.hopper.minigames

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import dev.helight.hopper.HopperPlugin
import dev.helight.hopper.api.MessageBuilder
import dev.helight.hopper.api.MessageContext
import dev.helight.hopper.hopper
import dev.helight.hopper.offstageAsync
import kotlinx.coroutines.channels.Channel
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.quartz.Job
import org.quartz.JobBuilder.newJob
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger

class Minigames : HopperPlugin {

    init {
        instance = this
    }

    var startJobKey = JobKey.jobKey("startJob", "minigames")
    val stateChannel = Channel<GameState> { }

    override fun enable() {
        offstageAsync {
            updateState(GameState.LOBBY)
            hopper.schedule(
                newJob(StartingJob::class.java)
                    .withIdentity(startJobKey)
                    .build(),
                newTrigger()
                    .withIdentity("startJobTrigger", "minigames")
                    .withSchedule(
                        simpleSchedule()
                            .repeatForever()
                            .withIntervalInMilliseconds(250)
                    )
                    .build()
            )
        }
    }

    override fun load() {

    }

    override fun disable() {

    }

    suspend fun start() {
        println("Starting Minigame")
        updateState(GameState.GAME)
        hopper.scheduler.deleteJob(startJobKey)
    }

    suspend fun updateState(state: GameState) {
        Minigames.state = state
        stateChannel.send(state)
    }

    companion object {
        lateinit var instance: Minigames
        var startingCondition = StartingCondition()
        var state = GameState.BOOTING
    }
}

@CommandAlias("minigames|mq")
class MinigamesCommand : BaseCommand() {

    @Subcommand("start")
    fun start() = offstageAsync {
        Minigames.instance.start()
    }

    @Subcommand("status")
    fun status(player: Player) {
        MessageBuilder(context = MessageContext(color = "§7", emphasize = "§e")) {
            embed("§8[§eMinigames§8] ") basic "Current Status: " emphasize Minigames.state.name
        }.send(player)
    }
}

class StartingJob : Job {

    override fun execute(context: JobExecutionContext) {
        if (Minigames.state != GameState.LOBBY) return
        if (!Minigames.startingCondition.requireManualStart) {
            if (Bukkit.getOnlinePlayers().size >= Minigames.startingCondition.minPlayers) {
                offstageAsync {
                    Minigames.instance.start()
                }
            }
        }
    }

}

class StartingCondition {
    var minPlayers = 1
    var requireManualStart = true
}

enum class GameState {
    BOOTING,
    LOBBY,
    GAME,
    ENDING
}