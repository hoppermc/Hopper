@file:OptIn(ExperimentalSerializationApi::class)

package dev.helight.hopper.ecs.impl.serializers

import dev.helight.hopper.ecs.ComponentSerializer
import dev.helight.hopper.ecs.craft.EcsItem
import dev.helight.hopper.ecs.craft.EcsMob
import dev.helight.hopper.ecs.craft.EcsPlayer
import dev.helight.hopper.ecs.impl.components.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HopperHealthSerializer : ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as HopperHealth)
    override fun deserialize(data: String): Any = Json.decodeFromString<HopperHealth>(data)
}

class HopperRegenSerializer : ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as HopperRegen)
    override fun deserialize(data: String): Any = Json.decodeFromString<HopperRegen>(data)
}

class HopperDamageSerializer : ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as HopperDamage)
    override fun deserialize(data: String): Any = Json.decodeFromString<HopperDamage>(data)
}

class BagComponentSerializer: ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as BagComponent)
    override fun deserialize(data: String): Any = Json.decodeFromString<BagComponent>(data)
}

class EntitySerializer: ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as EcsMob)
    override fun deserialize(data: String): Any = Json.decodeFromString<EcsMob>(data)
}

class DebugComponentSerializer: ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as DebugComponent)
    override fun deserialize(data: String): Any = Json.decodeFromString<DebugComponent>(data)
}

class SpigotItemSerializer : ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as EcsItem)
    override fun deserialize(data: String): Any = Json.decodeFromString<EcsItem>(data)
}

class SpigotPlayerSerializer : ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as EcsPlayer)
    override fun deserialize(data: String): Any = Json.decodeFromString<EcsPlayer>(data)
}

class IncreaseMaxHealthSerializer : ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as IncreaseMaxHealth)
    override fun deserialize(data: String): Any = Json.decodeFromString<IncreaseMaxHealth>(data)
}

class IncreaseRegenRateSerializer : ComponentSerializer {
    override fun serialize(value: Any?): String = Json.encodeToString(value as IncreaseRegenRate)
    override fun deserialize(data: String): Any = Json.decodeFromString<IncreaseRegenRate>(data)
}