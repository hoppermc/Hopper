package dev.helight.hopper.ecs

import dev.helight.hopper.*
import dev.helight.hopper.ecs.data.ExportedEntitySnapshot

/**
 * Wrapper around the exported entity triple for easier interaction
 *
 * Read operations return internal values unless stated otherwise in the name and docs
 * Write operations update global associated data values unless started otherwise in the name or the docs
 */
@Suppress("EXPERIMENTAL_API_USAGE")
@JvmInline
value class ExportedEntityWrapper(
    val entity: ExportedEntity
) {
    inline fun <reified T> get(): T {
        val id = T::class.java.toKey()
        val index = entity.second.indexOf(id)
        return entity.third[index] as T
    }

    inline fun <reified T> has() : Boolean {
        val id = T::class.java.toKey()
        return entity.second.contains(id)
    }

    inline fun <reified T> set(value: Any?) {
        ecs.set<T>(entityId, value)
    }

    inline fun <reified T> setInternal(value: Any?) {
        val id = T::class.java.toKey()
        val index = entity.second.indexOf(id)
        entity.third[index] = value
    }

    inline fun <reified T, reified TO> getAs(): TO {
        val id = T::class.java.toKey()
        val index = entity.second.indexOf(id)
        return entity.third[index] as TO
    }

    inline fun <reified T> project(): T = ComponentDataProjectionReflectors.getProjector<T>(entity.second).project(entity.third)

    val entityId: EntityId
        get() = entity.first

    val data: List<Pair<ComponentID, Any?>>
        get() = entity.second.mapIndexed { index, id ->
            id to entity.third[index]
        }.toList()

    fun snapshot() = ExportedEntitySnapshot.fromExported(entity)

    fun delete() {
        ecs.deleteEntity(entityId)
    }

    fun buffer() = BufferedEntity(entity)

}