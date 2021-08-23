package dev.helight.hopper.extensions

import org.bukkit.attribute.Attributable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

object EntityExtensions {

    val Entity.attributable: Attributable?
        get() = when (this) {
            is Attributable -> this
            else -> null
        }

    val Entity.living: LivingEntity?
        get() = when (this) {
            is LivingEntity -> this
            else -> null
        }


}