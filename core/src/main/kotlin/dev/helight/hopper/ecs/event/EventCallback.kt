package dev.helight.hopper.ecs.event

import dev.helight.hopper.ecs.ExportedEntityWrapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

@ExperimentalUnsignedTypes
class EventCallback {

    private val subscribers: MutableList<(ExportedEntityWrapper) -> Unit> = mutableListOf()
    private var arrived: ExportedEntityWrapper? = null

    fun subscribe(block: (ExportedEntityWrapper) -> Unit) {
        subscribers.add(block)
    }

    fun callback(exportedEntityWrapper: ExportedEntityWrapper) {
        subscribers.forEach { it(exportedEntityWrapper) }
        subscribers.clear()
        arrived = exportedEntityWrapper
    }

    suspend fun await(): ExportedEntityWrapper {
        if (arrived != null) return arrived!!
        val deferred = CompletableDeferred<ExportedEntityWrapper>()
        subscribe {
            deferred.complete(it)
        }
        return deferred.await()
    }

    fun awaitBlocking(): ExportedEntityWrapper = runBlocking { await() }
}