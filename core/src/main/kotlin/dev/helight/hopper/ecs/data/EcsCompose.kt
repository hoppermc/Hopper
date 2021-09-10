package dev.helight.hopper.ecs.data

import dev.helight.hopper.FilledComponent
import dev.helight.hopper.ecs
import dev.helight.hopper.toKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@ExperimentalUnsignedTypes
@Serializable
@SerialName("ecs:compose")
data class EcsCompose(
    val name: String,
    val entity: EntityProperties? = null,
    val components: List<EcsComposeEntry>
) {

    fun parseComponents(): List<FilledComponent> {
        return components.map { it.resolve() }
    }
}

@ExperimentalUnsignedTypes
@Serializable
@SerialName("ecs:compose:entry")
data class EcsComposeEntry(
    @SerialName("class") val clazz: String,
    val data: String
) {

    fun resolve(): FilledComponent {
        val id = ecs.classMapping[clazz]!!.toKey()
        val serializer = ecs.serializers[id]!!
        val deserialized = serializer.deserialize(data)
        return id to deserialized
    }

}