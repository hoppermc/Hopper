package dev.helight.hopper

import com.google.common.util.concurrent.MoreExecutors
import org.bukkit.Bukkit
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

private val threadGroup = ThreadGroup("OdysseusScheduling")
private val threadFactory = GroupedThreadFactory()
private val threadPool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(threadFactory))

internal fun shutdownOdysseusThreads() {
    threadGroup.interrupt()
}

fun Runnable.runAtLeastTrying(exceptionHandler: (Exception) -> Unit = {it.printStackTrace()}) {
    try {
        run()
    } catch (ex: Exception) {
        exceptionHandler(ex)
    }
}

class GroupedThreadFactory : ThreadFactory {

    override fun newThread(r: Runnable): Thread {
        return Thread(threadGroup) {
            r.runAtLeastTrying()
        }
    }
}

/**
 * Enqueues the block for the main thread and suspends execution until the task has been executed
 */
@HopperDsl
fun synchronize(block: Runnable) {
    val future = CompletableFuture<Nothing>()
    Bukkit.getScheduler().runTask(HopperSpigotHook.plugin, Runnable {
        block.run()
        future.complete(null)
    })
    future.get()
}

@HopperDsl
fun synchronizeDecoupled(block: Runnable) {
    Bukkit.getScheduler().runTask(HopperSpigotHook.plugin, block)
}

@HopperDsl
fun launch(block: Runnable) {
    threadPool.execute(block)
}

@HopperDsl
fun sleep(duration: Duration) {
    Thread.sleep(duration.toMillis())
}

@HopperDsl
fun sleepSeconds(seconds: Long) {
    sleep(Duration.ofSeconds(seconds))
}

/**
 * Launches a task in an separate non pooled thread
 * @param block the tasks that should be executed in an separate thread
 */
@HopperDsl
fun launchThread(block: Runnable): Thread {
    val thread = Thread {
        block.run()
    }
    thread.start()
    return thread
}

/**
 * Runs a suspending task repeating until the block returns false
 * @param delay initial delay before the task starts
 * @param period delay between the recurring execution of the block
 * @param block the suspendable task. The return value determines whether or not
 *              execution will be discontinued in the next cycle
 */
@HopperDsl
fun suspendRepeatingTask(delay: Long = 0, period: Long, block: () -> Boolean) {
    try {
        Thread.sleep(delay)
        while (block()) {
            Thread.sleep(period)
        }
    } catch (_: InterruptedException) {}
}

/**
 * Runs a task repeating until the block returns false
 * @param delay initial delay before the task starts
 * @param period delay between the recurring execution of the block
 * @param block the suspendable task. The return value determines whether or not
 *              execution will be discontinued in the next cycle
 */
@HopperDsl
fun launchRepeatingTask(delay: Long = 0, period: Long, block: () -> Boolean) = launch {
    try {
        Thread.sleep(delay)
        while (block()) {
            Thread.sleep(period)
        }
    } catch (_: InterruptedException) {}
}

/**
 * Runs a task repeating until the block returns false as a dedicated thread
 * @param delay initial delay before the task starts
 * @param period delay between the recurring execution of the block
 * @param block the suspendable task. The return value determines whether or not
 *              execution will be discontinued in the next cycle
 */
@HopperDsl
fun launchRepeatingTaskThreaded(delay: Long = 0, period: Long, block: () -> Boolean): Thread = launchThread {
    try {
        Thread.sleep(delay)
        while (block()) {
            Thread.sleep(period)
        }
    } catch (_: InterruptedException) {}
}