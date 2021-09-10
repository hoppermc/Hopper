package dev.helight.hopper.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.helight.hopper.*
import dev.helight.hopper.api.Item
import dev.helight.hopper.effects.Effects
import dev.helight.hopper.effects.Effects.cancelAfter
import dev.helight.hopper.entity.SpigotEntity
import dev.helight.hopper.extensions.PlayerExtensions.raycast
import dev.helight.hopper.extensions.VLBExtensions.destructible
import dev.helight.hopper.external.ColorApi
import dev.helight.hopper.inventory.v1.debug.ItemDataViewer
import dev.helight.hopper.utilities.Chat
import dev.helight.hopper.utilities.KVec.inverseTransform
import dev.helight.hopper.utilities.KVec.transformFast
import dev.helight.hopper.utilities.Raycast
import kotlinx.coroutines.launch
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.*


@ExperimentalUnsignedTypes
@CommandAlias("hopper")
@CommandPermission("hopper.use")
class HopperEngineCommand : BaseCommand() {


    @Subcommand("ink")
    @CommandPermission("hopper.ink")
    fun inkFlask(player: Player, hex: String) {
        decouple {
            val stripped = hex.removePrefix("#")
            val item = ColorApi.get(stripped).inkFlask()
            synchronizeDecoupled {
                player.inventory.addItem(item)
            }
        }
    }

    @Subcommand("itemdata")
    @CommandPermission("hopper.itemdata")
    fun nbtInfoItem(player: Player) {
        decouple {
            val item = player.inventory.itemInMainHand
            val viewer = ItemDataViewer(item)
            viewer.construct()
            synchronizeDecoupled {
                viewer.show(player)
            }
        }
    }

    @Subcommand("debug reflections")
    fun simulateReflection(player: Player) {
        val t1 = Instant.now()
        Raycast.castRaycast(player.eyeLocation, player.eyeLocation.clone().add(player.eyeLocation.direction.normalize().multiply(1)), Raycast.RaycastOptions(
            reflective = true,
            precision = 5,
            depth = 200,
            lineTracer = { a, b ->
                println("Drawing line between ${a.toVector().destructible} an ${b.toVector().destructible}")
                Effects.line(a, b, player, color = Color.RED).cancelAfter(Duration.ofSeconds(10))
            }
        ))
        val t2 = Instant.now()
        println("RayTracing took ${Duration.between(t1, t2).toMillis()}ms")
    }

    @Subcommand("debug entity")
    @CommandPermission("hopper.debug.entity")
    fun spawnEntity(player: Player) {
        val compose = File("entity.compose.json").readText()
        ecs.composer.executeLocational(compose, player.location)
    }

    @Subcommand("debug persistentEcs")
    @CommandPermission("hopper.debug.persistentEcs")
    fun debugPersistentEntity(player: Player) {
        ecs.create {
            add<DebugComponent>(DebugComponent("Persistent Entity ${UUID.randomUUID()}"))
        }
    }

    @Subcommand("debug item")
    @CommandPermission("hopper.debug.item")
    fun getItem(player: Player) {
        offstageAsync {
            val item = Item.builder(Material.APPLE).name("§cHopperApple").delegate()
            hopper.spigot.createItem(item) {
                add<DebugComponent>(DebugComponent(name = "HopperItem"))
            }

            launch(HopperDispatchers.SYNC) {
                player.location.world!!.dropItem(player.location, item)
            }
        }
    }

    @Subcommand("debug effect")
    @CommandPermission("hopper.debug.effect")
    fun testEffect(player: Player) {
        Effects.cuboidSelection(
            player.location.clone().subtract(0.5, 0.0, 0.5),
            player.location.clone().add(0.5, 2.0, 0.5), player,
            "Player Box"
        )
    }

    @Subcommand("debug protocolExplosion")
    @CommandPermission("hopper.debug.protocolExplosion")
    fun testProtocolLib(player: Player) {
        hopper.spigot.proto.playExplosion(player, player.location)
    }

    @Subcommand("debug protocolTitle")
    @CommandPermission("hopper.debug.protocolTitle")
    fun testProtocolLibTitle(player: Player) {
        val entities = Chat.showWorldTitle(
            player, player.location, duration = Duration.ofSeconds(5),
            msg = "Test1\nTest2 Test2\nTest3 Test3 Test3\nTest2 Test2\nTest1\nTest1\nTest2 Test2\nTest3 Test3 Test3\nTest2 Test2\nTest1"
        )
    }

    @Subcommand("debug camSwitch")
    @CommandPermission("hopper.debug.camSwitch")
    fun testCamSwitch(player: Player) {
        val entity = SpigotEntity.getNearbyEntities(player.raycast(), 0.5).first()
        hopper.spigot.proto.unsafeCamSwitch(player, entity)
    }

    @Subcommand("debug vecMatrix")
    @CommandPermission("hopper.debug.vecMatrix")
    fun vecMatrixTest(player: Player) {
        val eyeLocation = player.eyeLocation
        val offset = Vector(0.0, 0.0, 1.5)
        val p1 = eyeLocation transformFast offset
        val io = eyeLocation inverseTransform p1
        Effects.display(p1)
        println(p1)
        println(io)
    }

}