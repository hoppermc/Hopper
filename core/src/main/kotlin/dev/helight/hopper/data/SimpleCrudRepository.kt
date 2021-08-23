package dev.helight.hopper.data

interface SimpleCrudRepository<T: PersistentEntity> {

    suspend fun create(entity: T)
    suspend fun update(entity: T)
    suspend fun delete(entity: T)
    suspend fun get(id: String): T?
    suspend fun list(): List<T>

}