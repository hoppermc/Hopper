package dev.helight.hopper.ecs

interface ComponentSerializer {

    fun serialize(value: Any?): String
    fun deserialize(data: String): Any?

}

