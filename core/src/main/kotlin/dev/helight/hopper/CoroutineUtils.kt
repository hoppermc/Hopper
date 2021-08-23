package dev.helight.hopper

import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Bukkit

@HopperDsl
suspend fun synchronize(block: () -> Unit) {
    val deferred = CompletableDeferred<Any?>()
    Bukkit.getScheduler().runTask(HopperSpigotHook.plugin, Runnable {
        block()
        deferred.complete(null)
    })
    deferred.await()
}
