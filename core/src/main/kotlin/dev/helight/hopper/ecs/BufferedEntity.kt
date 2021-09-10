package dev.helight.hopper.ecs

import dev.helight.hopper.ExportedEntity
import dev.helight.hopper.TagComponent
import dev.helight.hopper.ecs
import dev.helight.hopper.extensions.ComponentGroupExtensions.migrateTo
import dev.helight.hopper.toKey

class BufferedEntity(
    var entity: ExportedEntity = ExportedEntity(ecs.newEntityId(), sortedSetOf(), mutableListOf())
) {

    inline fun <reified T> add(value: Any? = null) {
        val id = T::class.java.toKey()
        val newGroup = sortedSetOf(*entity.second.toTypedArray(), id)
        val newData = entity.second.migrateTo(newGroup, entity.third, addition = listOf(id to value))
        if (!ecs.serializers.containsKey(id)) ecs.registerDefaultSerializerForClass<T>()
        entity = entity.copy(second = newGroup, third = newData)
    }

    inline fun <reified T> tag() {
        if (!T::class.java.isAnnotationPresent(TagComponent::class.java)) error("Class must be annotated with @TagComponent")
        val value = T::class.java.newInstance()
        if (!ecs.serializers.containsKey(T::class.java.toKey())) ecs.registerDefaultSerializerForClass<T>()
        add<T>(value)
    }

    inline fun <reified T> set(value: Any? = null) {
        if (contains<T>()) {
            val index = entity.second.indexOf(T::class.java.toKey())
            entity = entity.copy(third = entity.third.toMutableList().apply {
                set(index, value)
            })
        } else {
            add<T>(value)
        }
    }

    inline fun <reified T> contains(): Boolean = entity.second.contains(T::class.java.toKey())

    fun push() {
        ecs.push(entity)
    }

    fun replace() = ecs.operation {
        ecs.deleteEntity(entity.first)
        ecs.push(entity)
    }

    fun readWrapper() = ExportedEntityWrapper(entity)

}