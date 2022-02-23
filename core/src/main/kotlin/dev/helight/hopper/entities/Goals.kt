package dev.helight.hopper.entities

import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import dev.helight.hopper.HopperSpigotHook
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import java.util.*

object HopperGoals {

    internal fun <T: Mob> followFor(mob: T) = GoalKey.of(mob.javaClass, NamespacedKey(HopperSpigotHook.plugin, "follow:${mob::class.java.name}"))
    internal fun <T: Mob> keepDistanceFor(mob: T) = GoalKey.of(mob.javaClass, NamespacedKey(HopperSpigotHook.plugin, "keep_distance:${mob::class.java.name}"))
    internal fun <T: Mob> floatFor(mob: T) = GoalKey.of(mob.javaClass, NamespacedKey(HopperSpigotHook.plugin, "float:${mob::class.java.name}"))

}

object VanillaGoalExtension {

}

interface LocationSelector<P: Mob> {
    fun select(mob: P): Location? = null
    operator fun invoke(mob: P) = select(mob)

    data class ConstantEntity<P: Mob>(
        val location: Location
    ) : LocationSelector<P> {
        override fun select(mob: P): Location = location
    }

    data class MutableEntity<P: Mob>(
        var location: Location?
    ) : LocationSelector<P> {
        override fun select(mob: P): Location? = location
    }

    class LambdaBased<P: Mob>(
        val block: P.() -> Location?
    ): LocationSelector<P> {
        override fun select(mob: P): Location? = block(mob)
    }
}

interface EntitySelector<P: Mob> {
    fun select(mob: P): LivingEntity? = null
    operator fun invoke(mob: P) = select(mob)

    data class ConstantEntity<P: Mob>(
        val entity: LivingEntity
    ) : EntitySelector<P> {
        override fun select(mob: P): LivingEntity = entity
    }

    data class MutableEntity<P: Mob>(
        var entity: LivingEntity?
    ) : EntitySelector<P> {
        override fun select(mob: P): LivingEntity? = entity
    }

    class ClassBased<P: Mob, E: LivingEntity>(
        val clazz: Class<E>,
        val radius: Double = 25.0
    ) : EntitySelector<P> {
        override fun select(mob: P): LivingEntity? = mob.getNearbyEntities(radius, radius, radius)
            .filterIsInstance(clazz)
            .minByOrNull { it.location.distance(mob.location) }
    }

    class LambdaBased<P: Mob>(
        val block: P.() -> LivingEntity?
    ): EntitySelector<P> {
        override fun select(mob: P): LivingEntity? = block(mob)
    }
}

class FollowGoal<T: Mob>(
    val mob: T,
    var selector: EntitySelector<T>,
    val speed: Double = 1.0
) : Goal<T> {

    var target: LivingEntity? = null

    private fun refreshTarget() {
        target = selector(mob)
    }

    override fun shouldActivate(): Boolean {
        refreshTarget()
        if (target == null) return false
        if (target!!.isDead) return false
        if (target!!.world != mob.world) return false
        return true
    }

    override fun tick() {
        mob.pathfinder.moveTo(target!!, speed)
        mob.lookAt(target!!)
    }

    override fun getKey() = HopperGoals.followFor(mob)
    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.LOOK)
}

class KeepDistanceGoal<T: Mob>(
    val mob: T,
    var selector: EntitySelector<T>,
    val distance: Double,
    val speed: Double = 1.0
) : Goal<T> {

    var target: LivingEntity? = null

    private fun refreshTarget() {
        target = selector(mob)
    }

    override fun shouldActivate(): Boolean {
        refreshTarget()
        if (target == null) return false
        if (target!!.isDead) return false
        if (target!!.world != mob.world) return false
        if (mob.location.distance(target!!.location) > distance) return false
        return true
    }

    override fun tick() {
        val currentDistance = mob.location.distance(target!!.location)
        val direction = mob.location.toVector().subtract(target!!.location.toVector()).normalize()
        mob.pathfinder.moveTo(mob.location.add(direction.multiply(distance - currentDistance)), speed)
    }

    override fun getKey(): GoalKey<T> = HopperGoals.keepDistanceFor(mob)
    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE)

}