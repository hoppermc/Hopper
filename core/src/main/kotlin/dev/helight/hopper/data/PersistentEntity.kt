package dev.helight.hopper.data

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

abstract class PersistentEntity {

    @BsonId
    open var id: String = ObjectId.get().toHexString()

    companion object {
        fun generateId(): String = ObjectId.get().toHexString()
    }

}