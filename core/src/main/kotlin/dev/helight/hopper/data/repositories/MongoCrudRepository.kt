package dev.helight.hopper.data.repositories

import dev.helight.hopper.data.PersistentEntity
import dev.helight.hopper.data.SimpleCrudRepository
import dev.helight.hopper.hopper
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine

class MongoCrudRepository<T: PersistentEntity>(val type: Class<T>) : SimpleCrudRepository<T> {

    @OptIn(ExperimentalUnsignedTypes::class)
    private var mongoCollection: CoroutineCollection<T> = hopper.mongoDatabase.database.getCollection(type.simpleName.lowercase(), type).coroutine

    override suspend fun create(entity: T) {
        mongoCollection.insertOne(entity)
    }

    override suspend fun update(entity: T) {
        val result = mongoCollection.updateOneById(entity.id, entity)
        if (result.matchedCount == 0L) error("No matches (${entity.id})")
    }

    override suspend fun delete(entity: T) {
        mongoCollection.deleteOneById(entity.id)
    }

    override suspend fun get(id: String): T? {
        return mongoCollection.findOneById(id)
    }

    override suspend fun list(): List<T> {
        return mongoCollection.find().toList()
    }

}