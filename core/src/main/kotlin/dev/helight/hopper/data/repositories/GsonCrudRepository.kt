package dev.helight.hopper.data.repositories

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.helight.hopper.data.PersistentEntity
import dev.helight.hopper.data.SimpleCrudRepository
import java.io.File

class GsonCrudRepository<T: PersistentEntity>(val type: Class<T>) : SimpleCrudRepository<T> {

    val gson: Gson = GsonBuilder().create()
    var map: MutableMap<String, T> = mutableMapOf()
    val data = File("data")
    val file = File("data", type.simpleName.lowercase().plus(".json"))
    init {
        if (data.exists()) data.mkdir()
        if (file.exists()) {
            val reader = file.reader()
            val mapTypeToken = object : TypeToken<MutableMap<String, T>>(){}.type
            map = gson.fromJson(reader, mapTypeToken)
            reader.close()
        } else {
            file.createNewFile()
            write()
        }
    }

    private fun write() {
        val writer = file.writer()
        gson.toJson(map, writer)
        writer.close()
    }

    override suspend fun create(entity: T) {
        map[entity.id] = entity
        write()
    }

    override suspend fun update(entity: T) {
        map[entity.id] = entity
        write()
    }

    override suspend fun delete(entity: T) {
        map[entity.id] = entity
        write()
    }

    override suspend fun get(id: String): T? {
        return map.getOrDefault(id, null)
    }

    override suspend fun list(): List<T> {
        return map.values.toList()
    }

}