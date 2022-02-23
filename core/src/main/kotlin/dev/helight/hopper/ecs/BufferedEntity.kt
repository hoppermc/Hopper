package dev.helight.hopper.ecs

import dev.helight.hopper.*
import dev.helight.hopper.extensions.ComponentGroupExtensions.migrateTo

class BufferedEntity(
    var entity: ExportedEntity = ExportedEntity(ecs.newEntityId(), sortedSetOf(), mutableListOf()),
    var hasMigrated: Boolean = false,
    var changedComponents: MutableSet<ComponentID> = mutableSetOf()
) {

    inline fun <reified T> add(value: Any? = null) {
        hasMigrated = true
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
            val component = T::class.java.toKey()
            changedComponents.add(component)
            val index = entity.second.indexOf(component)
            entity = entity.copy(third = entity.third.toMutableList().apply {
                set(index, value)
            })
        } else {
            add<T>(value)
        }
    }

    inline fun <reified T> updateEntity(value: Any? = null) {
        if (contains<T>()) {
            val component = T::class.java.toKey()
            changedComponents.add(component)
            val index = entity.second.indexOf(component)
            entity = entity.copy(third = entity.third.toMutableList().apply {
                set(index, value)
            })
        } else {
            error("Component doesn't currently exist in the entity")
        }
    }

    inline fun <reified T> contains(): Boolean = entity.second.contains(T::class.java.toKey())

    fun pushEntity() {
        ecs.push(entity)
    }

    fun replaceEntity() {
        if (!hasMigrated) updateEntity()
        ecs.storage.replaceEntity(entity)
    }

    fun updateEntity() {
        if (hasMigrated) error("Can't update because the component groups has migrated")
        changedComponents.forEach {
            val index = entity.second.indexOf(it)
            ecs.storage.updateComponent(entity.first, it, entity.third[index])
        }
    }

    fun readWrapper() = ExportedEntityWrapper(entity)

}