package dev.helight.hopper.ecs

import com.google.gson.Gson
import com.google.gson.JsonElement

interface ComponentSerializer {

    fun serialize(value: Any?): String
    fun deserialize(data: String): Any?

}

class GsonComponentSerializer<T>(val clazz: Class<T>) : ComponentSerializer {

    var gson = Gson()

    constructor(clazz: Class<T>, gson: Gson) : this(clazz) {
        this.gson = gson
    }

    override fun serialize(value: Any?): String {
        return gson.toJson(value)
    }

    override fun deserialize(data: String): Any? {
        val tree = gson.fromJson(data, JsonElement::class.java)
        if (tree.isJsonNull) return null
        return gson.fromJson(tree, clazz)
    }


}
