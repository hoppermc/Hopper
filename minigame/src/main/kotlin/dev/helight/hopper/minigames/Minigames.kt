package dev.helight.hopper.minigames

import dev.helight.hopper.HopperPlugin
import dev.helight.hopper.hopper
import org.quartz.Job
import org.quartz.JobBuilder.newJob
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger

class Minigames : HopperPlugin {

    var startJobKey = JobKey.jobKey("startJob", "minigames")

    override fun disable() {
        TODO("Not yet implemented")
    }

    override fun enable() {
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

    override fun load() {
        TODO("Not yet implemented")
    }

    fun start() {
        hopper.scheduler.deleteJob(startJobKey)
        state = GameState.GAME
    }

    companion object {
        var startingCondition = StartingCondition()
        var state = GameState.BOOTING
    }
}

class StartingJob : Job {

    override fun execute(context: JobExecutionContext?) {
        TODO("Not yet implemented")
    }

}

class StartingCondition {
    var minPlayers = 1
    var requireManualStart = false
}

enum class GameState {
    BOOTING,
    LOBBY,
    GAME,
    ENDING
}