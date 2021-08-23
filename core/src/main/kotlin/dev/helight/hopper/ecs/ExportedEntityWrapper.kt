package dev.helight.hopper.ecs

import dev.helight.hopper.*
import dev.helight.hopper.ecs.data.ExportedEntitySnapshot

@ExperimentalUnsignedTypes
@JvmInline
value class ExportedEntityWrapper(
    val entity: ExportedEntity
) {
    inline fun <reified T> get(): T {
        val id = T::class.java.toKey()
        val index = entity.second.indexOf(id)
        return entity.third[index] as T
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

}