package dev.helight.hopper.inventory.v1

import dev.helight.hopper.inventory.v1.annotation.NoGarbageCollection
import dev.helight.hopper.synchronizeDecoupled
import org.quartz.Job
import org.quartz.JobExecutionContext


class GuiGarbageCollector : Job {
    fun collect() {
        for (value in Gui.cache.values) {
            if (value.javaClass.isAnnotationPresent(NoGarbageCollection::class.java)) {
                continue
            }
            val delay: Long = System.nanoTime() - value.lastAction
            if (delay >= EVICTION_TIMEOUT) {
                println(String.format("GuiGarbageCollector is closing inventory with id %s for being inactive too long",
                    value.id.toString()))
                synchronizeDecoupled { value.dispose(Gui.DisposeReason.GARBAGE_COLLECTOR) }
            }
        }
    }

    override fun execute(context: JobExecutionContext?) {
        collect()
    }

    companion object {
        private const val EVICTION_TIMEOUT = 1000000000L * 10
    }
}

