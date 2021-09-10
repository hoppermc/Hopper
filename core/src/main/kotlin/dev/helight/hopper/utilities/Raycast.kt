package dev.helight.hopper.utilities

import dev.helight.hopper.extensions.VLBExtensions.center
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object Raycast {

    fun reflectionCollisionRaycast(origin: Location, target: Location, depth: Int = 5, block: (origin: Location, hit: Location) -> Boolean) {
        if (depth == 0) {
            println("Max Depth Reached")
            return
        }

        val result = castRaycast(origin, target, RaycastOptions(depth = 100, collidesWithEntity = {
            true
        }, checkEntities = true, collidesWithBlock = {
            it.type.isSolid && block != origin.block
        }))

        val hit: Location = result.hit
        val reflectionTarget = reflective(origin, hit)
        println("Raycast started at $origin and hit $hit")
        println("Reflection target from $hit to $reflectionTarget")
        if (!block(origin, hit)) {
            println("Reflection finished")
            return
        }
        reflectionCollisionRaycast(hit, reflectionTarget, depth - 1, block)
    }

    fun reflectionOccludingRaycast(origin: Location, target: Location, depth: Int = 5, block: (origin: Location, hit: Location) -> Boolean) {
        if (depth == 0) {
            println("Max Depth Reached")
            return
        }

        val result = castRaycast(origin, target, RaycastOptions(depth = 100, collidesWithEntity = {
            true
        }, checkEntities = true, collidesWithBlock = {
            it.type.isOccluding && block != origin.block
        }))

        val hit: Location = result.hit
        val reflectionTarget = reflective(origin, hit)
        println("Raycast started at $origin and hit $hit")
        println("Reflection target from $hit to $reflectionTarget")
        if (!block(origin, hit)) {
            println("Reflection finished")
            return
        }
        reflectionCollisionRaycast(hit, reflectionTarget, depth - 1, block)
    }

    private fun reflective(origin: Location, hit: Location): Location {
        val block = hit.block
        val vec2 = VectorUtils.getFace(hit, block.center)
        val layerPointAtEye = VectorUtils.replaceZero(vec2, origin, hit)
        val distance = layerPointAtEye.clone().subtract(origin).length()
        val p1 = hit.clone().add(vec2.clone().multiply(distance))
        val vec3 = p1.clone().subtract(origin).multiply(2.0)
        return origin.clone().add(vec3)
    }


    fun castColliderRaycast(player: Player, precision: Int = 5, maxDepth: Int = 32, colluding: Boolean = true, collideWithEntities: Boolean = true): Location {
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
            if (collideWithEntities && block.world.getNearbyEntities(current,
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

    fun castColliderRaycast(a: Location, b: Location, precision: Int = 5, maxDepth: Int = 32, excludeCollisionWith: Block? = null,  colluding: Boolean = true, collideWithEntities: Boolean = true): Location {
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
            if (block.type.isSolid && block != excludeCollisionWith) iterate = false
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

    fun castRaycast(a: Location, b: Location, options: RaycastOptions): RaycastResult {
        if (options.reflective) {
            var depth = options.reflectionDepth
            var o: Location = a
            var t: Location = b
            var result = RaycastResult(o, o, 0)
            while (depth != 0) {
                depth -= 1
                val originExcludedCollider = { it: Block ->
                    o.block != it && options.collidesWithBlock(it)
                }
                result = castInternal(o, t, options.copy(collidesWithBlock = originExcludedCollider))
                options.lineTracer(o, result.hit)
                val reflectionTarget = reflective(o, result.hit)
                val ref = options.doesReflect(result.hit, reflectionTarget)

                o = result.hit
                t = ref.second
                if (!ref.first) break
            }
            return result
        } else {
            return castInternal(a, b, options)
        }
    }

    private fun castInternal(a: Location, b: Location, options: RaycastOptions): RaycastResult {
        val multiply = 1f / options.precision
        val halfMultiply = multiply / 2f
        val origin = a.clone()
        val current = origin.clone()
        val direction = b.clone().subtract(a).toVector().normalize().multiply(multiply)
        var iterate = true
        var depth = 0
        while (iterate && depth < options.depth) {
            depth++
            val block = current.block
            if (options.collidesWithBlock(block)) iterate = false
            if (options.checkEntities && block.world.getNearbyEntities(current,
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble()).isNotEmpty()
            ) iterate = false
            if (!iterate) continue
            current.add(direction)
        }
        val optimizerDirection = direction.multiply(0.25)
        val optimizerCurrent = current.clone()
        var optimizerLast = current.clone()
        var optimizerIterate = true
        while (optimizerIterate) {
            val block = optimizerCurrent.block
            if (!options.collidesWithBlock(block)) optimizerIterate = false
            if (options.checkEntities && block.world.getNearbyEntities(current,
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble(),
                    halfMultiply.toDouble()).isEmpty()
            ) optimizerIterate = false
            if (!optimizerIterate) continue

            optimizerLast = optimizerCurrent.clone()
            optimizerCurrent.subtract(optimizerDirection)
        }
        return RaycastResult(optimizerLast, optimizerCurrent, depth)
    }

    data class RaycastOptions(
        val depth: Int = 100,
        val precision: Int = 5,
        val checkEntities: Boolean = false,
        val reflective: Boolean = false,
        val reflectionDepth: Int = 5,
        val doesReflect: (hit: Location, target: Location) -> Pair<Boolean, Location> = { _, target -> true to target },
        val collidesWithBlock: (Block) -> Boolean = { it.type.isSolid },
        val collidesWithEntity: (Entity) -> Boolean = { true },
        val lineTracer: (a: Location, b: Location) -> Unit = { _, _ ->}
    )

    data class RaycastResult(
        val hit: Location,
        val last: Location,
        val depth: Int
    )

}