package dev.helight.hopper.example

import dev.helight.hopper.*
import dev.helight.hopper.ecs.ComponentSerializer
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.system.HopperSystem
import dev.helight.hopper.ecs.system.HopperSystemOptions
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@ExperimentalUnsignedTypes
@AutoConfigurePlugin
class ExampleHopperPlugin : HopperPlugin {

    override fun disable() {
        println("Disabled")
    }

    override fun enable() {
        println("Enabled")
        ecs.system<ExampleSystem>()
        println(ExampleComponent::class.java.name)
       // Composer().executeCompose(File("exampleCompose.json").readText())
    }

    override fun load() {
        //ecs.serializer<ExampleComponent>(ExampleComponentSerializer())
    }
}

class ExampleComponentSerializer : ComponentSerializer {
    override fun deserialize(data: String): Any {
        return Json.decodeFromString<ExampleComponent>(data)
    }

    override fun serialize(value: Any?): String {
        val component: ExampleComponent? = value as ExampleComponent?
        return Json.encodeToString(component)
    }
}

@ExperimentalUnsignedTypes
class ExampleSystem : HopperSystem(sortedSetOf(ExampleComponent::class.java.toKey()), HopperSystemOptions(isTicking = true)) {

    override fun tickIndividual(entity: ExportedEntityWrapper) {
        val data = entity.get<ExampleComponent>()
        println("Ticking $data")
        entity.set<ExampleComponent>(data.copy(age = data.age + 1))
        println("Updated Data")
    }

}

@Serializable
@AutoComponent(ExampleComponentSerializer::class)
data class ExampleComponent(
    val name: String,
    val age: Int
)