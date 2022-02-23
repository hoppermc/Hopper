package dev.helight.hopper.ecs.impl.components

import dev.helight.hopper.EntityId
import dev.helight.hopper.TagComponent
import dev.helight.hopper.extensions.VLBExtensions.vec3
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Entity

/**
 * Marks an entity as required to be worn in order to have an effect.
 * This Tag has no concrete implementation and just exists to create a common
 * api surface. The tag should be use with an entity, also having the **SpigotItem**
 * component.
 *
 * @see dev.helight.hopper.ecs.craft.EcsItem
 */
@TagComponent
class ActiveWearable {}

/**
 * Prints the entity data and id to system out, every hopper tick, for each instance.
 *
 * @param[name] name shown in logs
 */
@Serializable
@SerialName("hopper:debug")
data class DebugComponent(
    val name: String
)

/**
 * Internal tag which is automatically assigned to each invoked event.
 */
@TagComponent
class HopperEvent {}

/**
 * Makes entities in metadata environments continuously update
 * their metadata values to reflect the current state snapshot.
 *
 * Since saving the exported snapshot requires serialization,
 * this tag should be used with caution and only where required
 * to prevent increased hopper tick frame times
 */
@TagComponent
class RollingMetaStorage {}

@TagComponent
class DetailedItemInfos {}

/**
 * Data component for the **alternate health system**.
 *
 * @param[health] current amount of health
 * @param[baseMaxHealth] base upper limit for health
 * @param[maxHealth] current max amount of health, usually computed internally
 *
 * @property[isDead] checks if the health is below 0 to determine if the entity is dead.
 */
@Serializable
@SerialName("hopper:health")
data class HopperHealth(
    val health: Double,
    val maxHealth: Double,
    val baseMaxHealth: Double = 20.0,
    val lastDamage: HopperDamage = HopperDamage(0.0, damageType = -1, sourceType = -1, null, Triple(0f,0f,0f), Clock.System.now())
) {
    val isDead: Boolean
        get() = health <= 0
}

@Serializable
@SerialName("hopper:increase:health")
data class IncreaseMaxHealth(
    val increase: Double
)

/**
 * Data component for bag like **items, holding individual inventories**.
 * The inventory will have an amount of 9 times the [rows] as slots
 *
 * @param[title] name of the inventory
 * @param[rows] amount of rows
 * @param[data] internal base64 value representing the serialized contents of the inventory
 */
@Serializable
@SerialName("hopper:bag")
data class BagComponent(
    val title: String,
    val rows: Int = 3,
    val data: String = ""
)

/**
 * Data component for **passive health regeneration**, when used together with [HopperHealth]
 * The amount of health regenerated is computed by multiplying the current rate with
 * [deltaTime][dev.helight.hopper.ecs.system.HopperSystemTicker.deltaTime] and then dividing
 * the result by [tickRate][dev.helight.hopper.ecs.system.HopperSystemTicker.tickRate],
 * creating a ticks skip independent healing rate per second.
 *
 * @param[baseRate] normal regeneration/sec
 * @param[rate] current regeneration/sec. This will usually be internally computed
 */
@Serializable
@SerialName("hopper:regen")
data class HopperRegen(
    val baseRate: Double,
    val rate: Double
) {
    companion object {
        /**
         * @see HopperRegen
         */
        fun create(baseRate: Double): HopperRegen {
            return HopperRegen(baseRate,baseRate)
        }
    }
}

@Serializable
@SerialName("hopper:increase:regen")
data class IncreaseRegenRate(
    val increase: Double
)

/**
 * Data component for the **damage** that has been dealt to an entity with [HopperHealth].
 *
 * @param[damage] amount of damage
 * @param[damageType] type of damage or, when induced by bukkit,
 * the ordinal of [DamageCause][org.bukkit.event.entity.EntityDamageEvent.DamageCause]
 * @param[sourceType] type of the damager or, when induced by bukkit,
 * the ordinal of [EntityType][org.bukkit.entity.EntityType]
 * @param[source] id of damager in case it is a hopper entity.
 * @param[knockback] knockback of the attack as a float triple serialized Vector3
 */
@Serializable
@SerialName("hopper:damage")
data class HopperDamage(
    val damage: Double,
    val damageType: Int,
    val sourceType: Int,
    val source: EntityId?,
    val knockback: Triple<Float, Float, Float>,
    val timestamp: Instant
) {
    companion object {
        /**
         * @see HopperDamage
         *
         * @param[entity] entity being damaged
         * @param[damager] entity attacking the [entity]
         */
        fun eveAttack(entity: Entity, damager: Entity, damage: Double, damageType: Int = 0, sourceType: Int = 0, knockbackDistance: Double = 0.5, source: EntityId? = null): HopperDamage {
            val a = damager.location.toVector().vec3
            val b = entity.location.toVector().vec3
            val c = (b - a).normalize() * knockbackDistance
            return HopperDamage(damage, damageType, sourceType, source, c.floatTriple, Clock.System.now())
        }

        /**
         * @see HopperDamage
         */
        fun instantAttack(damage: Double, damageType: Int = 0, sourceType: Int = 0, source: EntityId? = null): HopperDamage {
            (1 + 1)
            return HopperDamage(damage, damageType, sourceType, source, Triple(0f,0f,0f), Clock.System.now())
        }
    }
}