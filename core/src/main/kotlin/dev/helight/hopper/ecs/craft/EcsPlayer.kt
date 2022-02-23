package dev.helight.hopper.ecs.craft

import dev.helight.hopper.ecs
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.toKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@Serializable
@SerialName("hopper:player")
data class EcsPlayer(
    val uuid: String
) {

    fun get(): Player? {
        return Bukkit.getPlayer(UUID.fromString(uuid))
    }

    companion object {
        fun all(): List<ExportedEntityWrapper> {
            return ecs.queryExpanded(TreeSet(setOf(EcsPlayer::class.java.toKey())))
        }

        internal fun fromPlayer(player: Player): EcsPlayer = EcsPlayer(player.uniqueId.toString())

        fun of(player: Player): ExportedEntityWrapper {
            val uidString = player.uniqueId.toString()
            return all().first { it.get<EcsPlayer>().uuid == uidString }
        }
    }
}