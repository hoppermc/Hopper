package dev.helight.hopper.data.repositories

import dev.helight.hopper.data.PersistentEntity
import dev.helight.hopper.data.SimpleCrudRepository

class MemoryCrudRepository<T: PersistentEntity> : SimpleCrudRepository<T> {

    var map: MutableMap<String, T> = mutableMapOf()

    override suspend fun create(entity: T) {
        map[entity.id] = entity
    }

    override suspend fun update(entity: T) {
        map[entity.id] = entity
    }

    override suspend fun delete(entity: T) {
        map[entity.id] = entity
    }

    override suspend fun get(id: String): T? {
        return map.getOrDefault(id, null)
    }

    override suspend fun list(): List<T> {
        return map.values.toList()
    }

}