package dev.helight.hopper.ecs.system

import dev.helight.hopper.decouple
import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.event.EventCallback
import dev.helight.hopper.ecs.event.HopperEvent
import dev.helight.hopper.toKey
import kotlinx.coroutines.runBlocking
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Phaser
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@OptIn(ExperimentalUnsignedTypes::class)
class HopperSystemTicker : Job {

    override fun execute(context: JobExecutionContext) = runBlocking {
        val success = lock.tryLock(1, TimeUnit.SECONDS)
        if (!success) {
            println("Hopper System held locked for too long, skipping frame")
            return@runBlocking
        }
        val timestampBefore = Instant.now()
        try {
            val events = ecs.storage.peekAll(sortedSetOf(HopperEvent::class.java.toKey()))
            events.forEach { ee ->
                ecs.eventHandlers.all().filter {
                    it.componentGroup.intersect(ee.second).size == it.componentGroup.size
                }.sortedByDescending {
                    it.priority
                }.forEach { handler ->
                    try {
                        val current = ecs.get(ee.first) ?: error("Event has been deleted while still being processed")
                        handler.handle(current)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }

            events.forEach {
                val ee = ExportedEntityWrapper(ecs.storage.removeEntity(it.first)!!)
                if (ee.has<EventCallback>()) {
                    val callback = ee.get<EventCallback>()
                    // Completely decoupled from tick rate to prevent weird freezing errors,
                    // using the internal hopper thread pool which isn't connected to kotlinx coroutines.
                    // synchronisation can be reestablished by using startOperation and stopOperation
                    decouple {
                        callback.callback(ee)
                    }
                }
            }

            ecs.systems.all().forEach { system ->
                try {
                    if (system.options.isTicking) {
                        when(system.options.expanded) {
                            true -> system.tick(ecs.queryExpanded(system.componentGroup))
                            false -> system.tick(ecs.query(system.componentGroup))
                        }
                    }
                } catch (ex: Exception) {
                    val systemName = system.javaClass.simpleName
                    println("Error in System $systemName")
                    ex.printStackTrace()
                }
            }
        } finally {
            val timestampAfter = Instant.now()
            val tickDuration = Duration.between(timestampBefore, timestampAfter).toMillis()
            //println("Tick took $tickDuration ms")
            lock.unlock()
        }
    }

    companion object {
        val lock = ReentrantLock()
        val phaser = Phaser(1)

        fun startOperation() {
            phaser.register()
        }

        fun stopOperation() {
            phaser.arriveAndDeregister()
        }

        fun awaitNextTick() {
            phaser.awaitAdvance(phaser.phase)
        }
    }
}

