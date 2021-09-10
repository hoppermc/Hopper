package dev.helight.hopper.ecs

import dev.helight.hopper.EntityId
import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.data.EcsCompose
import dev.helight.hopper.entity.SpigotEntity
import dev.helight.hopper.hopper
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.Location

class Composer {

    @ExperimentalUnsignedTypes
    fun executeLocational(content: String, location: Location): EntityId {
        val compose = Json.decodeFromString<EcsCompose>(content)
        val components = compose.parseComponents()

        val entity: EntityId = if (compose.entity != null) {
            val id = hopper.spigot.spawnEntity(location, compose.entity.type)
            compose.entity.applyOn(ecs.get(id)!!.get<SpigotEntity>().resolve()!!)
            id
        } else {
            ecs.createEntityWithOperation()
        }

        components.forEach {
            ecs.storage.addComponent(entity, it.first, it.second)
        }

        return entity
    }

}