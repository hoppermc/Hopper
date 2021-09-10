package dev.helight.hopper.ecs.event

import dev.helight.hopper.TagComponent
import dev.helight.hopper.ecs.ExportedEntityWrapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

@TagComponent
class HopperEvent {}

interface Event {}

@ExperimentalUnsignedTypes
class EventCallback {

    private val subscribers: MutableList<(ExportedEntityWrapper) -> Unit> = mutableListOf()

    fun subscribe(block: (ExportedEntityWrapper) -> Unit) {
        subscribers.add(block)
    }

    fun callback(exportedEntityWrapper: ExportedEntityWrapper) {
        subscribers.forEach { it(exportedEntityWrapper) }
        subscribers.clear()
    }

    suspend fun await(): ExportedEntityWrapper {
        val deferred = CompletableDeferred<ExportedEntityWrapper>()
        subscribe {
            deferred.complete(it)
        }
        return deferred.await()
    }

    fun awaitBlocking(): ExportedEntityWrapper = runBlocking { await() }
}