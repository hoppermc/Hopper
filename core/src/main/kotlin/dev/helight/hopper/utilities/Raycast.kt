package dev.helight.hopper.utilities

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object Raycast {

    fun castColliderRaycast(player: Player, precision: Int = 5, maxDepth: Int = 32, colluding: Boolean = true): Location {
        val multiply = 1f / precision
        val halfMultiply = multiply / 2f
        val origin = player.eyeLocation
        val current = origin.clone()
        var last = origin.clone()
        val eyeDirection = player.eyeLocation.direction.normalize().multiply(multiply)
        var iterate = true
        var depth = 0
        while (iterate && depth < maxDepth) {
            depth++
            val block = current.block
            if (block.type.isSolid) iterate = false
            if (block.world.getNearbyEntities(current,
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble()).stream().anyMatch { entity: Entity -> entity.entityId != player.entityId }
            ) iterate = false
            if (!iterate) continue
            last = current.clone()
            current.add(eyeDirection)
        }
        return if (colluding) current else last
    }

    fun castColliderRaycast(a: Location, b: Location, precision: Int = 5, maxDepth: Int = 32, colluding: Boolean = true, collideWithEntities: Boolean = true): Location {
        val multiply = 1f / precision
        val halfMultiply = multiply / 2f
        val origin = a.clone()
        val current = origin.clone()
        var last = origin.clone()
        val direction = b.clone().subtract(a).toVector().normalize().multiply(multiply)
        var iterate = true
        var depth = 0
        while (iterate && depth < maxDepth) {
            depth++
            val block = current.block
            if (block.type.isSolid) iterate = false
            if (collideWithEntities && block.world.getNearbyEntities(current,
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble()).isNotEmpty()
            ) iterate = false
            if (!iterate) continue
            last = current.clone()
            current.add(direction)
        }
        return if (colluding) current else last
    }

    fun castColliderRaycast(a: Location, b: Location, excludeCollision: List<Entity>, precision: Int = 5, maxDepth: Int = 32, colluding: Boolean = true): Location {
        val multiply = 1f / precision
        val halfMultiply = multiply / 2f
        val origin = a.clone()
        val current = origin.clone()
        var last = origin.clone()
        val direction = b.clone().subtract(a).toVector().normalize().multiply(multiply)
        var iterate = true
        var depth = 0
        while (iterate && depth < maxDepth) {
            depth++
            val block = current.block
            if (block.type.isSolid) iterate = false
            if (block.world.getNearbyEntities(current,
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble()).any { entity: Entity -> !excludeCollision.contains(entity) }
            ) iterate = false
            if (!iterate) continue
            last = current.clone()
            current.add(direction)
        }
        return if (colluding) current else last
    }

}