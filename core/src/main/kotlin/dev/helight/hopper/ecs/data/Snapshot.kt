package dev.helight.hopper.ecs.data

import dev.helight.hopper.ExportedEntity
import dev.helight.hopper.ecs
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("ecs:snapshot")
data class EcsSnapshot(
    val idCounterSnapshot: ULong,
    val storeSnapshot: StoreSnapshot
)

@Serializable
@SerialName("ecs:snapshot:store")
data class StoreSnapshot(
    val archetypes: List<ArchetypeSnapshot>
)

@Serializable
@SerialName("ecs:snapshot:archetype")
data class ArchetypeSnapshot(
    val group: String,
    val entities: List<EntitySnapshot>
)

@Serializable
@SerialName("ecs:snapshot:entity")
data class EntitySnapshot(
    val id: ULong,
    val data: List<String>
)

@Serializable
@SerialName("ecs:snapshot:exported")
data class ExportedEntitySnapshot(
    val id: ULong,
    val group: String,
    val data: List<String>
) {
    @ExperimentalUnsignedTypes
    companion object {
        fun fromExported(exported: ExportedEntity): ExportedEntitySnapshot {
            return ExportedEntitySnapshot(exported.first, exported.second.joinToString("-"), exported.third.mapIndexed { index, it ->
                val id = exported.second.toList()[index]
                ecs.serializers[id]!!.serialize(it)
            })
        }

        fun toExported(snapshot: ExportedEntitySnapshot): ExportedEntity {
            val group = TreeSet(snapshot.group.split("-").map { it.toULong() })
            return Triple(snapshot.id, group, snapshot.data.mapIndexed { index, it ->
                val id = group.toList()[index]
                println("Deserializing id $id")
                ecs.serializers[id]!!.deserialize(it)
            }.toMutableList())
        }

    }
}