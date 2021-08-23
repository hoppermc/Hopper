package dev.helight.hopper.ecs

import dev.helight.hopper.ecs
import org.quartz.Job
import org.quartz.JobExecutionContext

@OptIn(ExperimentalUnsignedTypes::class)
class HopperSystemTicker : Job {

    override fun execute(context: JobExecutionContext) {
        ecs.systems.all().forEach {
            try {
                if (it.options.isTicking) it.tick(ecs.query(it.componentGroup))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}

