package dev.helight.hopper.ecs.data

import dev.helight.hopper.extensions.EntityExtensions.attributable
import dev.helight.hopper.extensions.EntityExtensions.living
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

@Serializable
@SerialName("mc:entity:properties")
data class EntityProperties(
    val type: EntityType,
    val customName: String? = null,
    val health: Double? = null,
    val speed: Double? = null,
    val damage: Double? = null,
    val knockback: Double? = null,
    val followRange: Double? = null,
    val armor: Double? = null,
    val armorToughness: Double? = null,
) {
    fun construct() {

    }

    companion object {
        fun parseFormString(content: String): EntityProperties = Json.decodeFromString(content)
    }

    fun applyOn(entity: Entity) {
        if (customName != null) {
            entity.customName = customName
            entity.isCustomNameVisible = true
        }
        if (health != null) {
            entity.attributable!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = health
            entity.living!!.health = health
        }
        if (speed != null) entity.attributable!!.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!.baseValue = speed
        if (damage != null) entity.attributable!!.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.baseValue = damage
        if (knockback != null) entity.attributable!!.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)!!.baseValue = knockback
        if (followRange != null) entity.attributable!!.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)!!.baseValue = followRange
        if (armor != null) entity.attributable!!.getAttribute(Attribute.GENERIC_ARMOR)!!.baseValue = armor
        if (armorToughness != null) entity.attributable!!.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)!!.baseValue = armorToughness
    }
}

