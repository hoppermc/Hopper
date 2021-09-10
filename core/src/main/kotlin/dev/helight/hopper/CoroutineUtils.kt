package dev.helight.hopper

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class HopperSyncCoroutineDispatcher: CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        Bukkit.getScheduler().runTask(HopperSpigotHook.plugin, block)
    }
}



object HopperDispatchers {
    val SYNC = HopperSyncCoroutineDispatcher()
    val ASYNC = Executors.newCachedThreadPool().asCoroutineDispatcher()
}

//@HopperDsl
//suspend fun synchronize(block: () -> Unit) {
//    val deferred = CompletableDeferred<Any?>()
//    Bukkit.getScheduler().runTask(HopperSpigotHook.plugin, Runnable {
//        block()
//        deferred.complete(null)
//    })
//    deferred.await()
//}

@HopperDsl
suspend fun synchronize(block: suspend CoroutineScope.() -> Unit): Unit = CoroutineScope(HopperDispatchers.SYNC).launch {
    block()
}.join()

/**
 * Starts a coroutine which is not connected to its parent's context
 * therefore allowing it to exit without waiting for it to finish
 */
fun CoroutineScope.offstage(
    context: CoroutineContext = HopperDispatchers.ASYNC,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return CoroutineScope(context).launch(block = block)
}

fun offstageAsync(block: suspend CoroutineScope.() -> Unit) = CoroutineScope(HopperDispatchers.ASYNC).launch {
    block()
}

fun offstageSync(block: suspend CoroutineScope.() -> Unit) = CoroutineScope(HopperDispatchers.SYNC).launch {
    block()
}