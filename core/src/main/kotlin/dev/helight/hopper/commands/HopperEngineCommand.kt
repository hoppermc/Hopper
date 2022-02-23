package dev.helight.hopper.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import com.comphenix.protocol.utility.MinecraftReflection
import dev.helight.hopper.*
import dev.helight.hopper.api.*
import dev.helight.hopper.api.TextComponents.combined
import dev.helight.hopper.api.TextComponents.padEnd
import dev.helight.hopper.api.TextComponents.with
import dev.helight.hopper.data.repositories.HttpBlobRepository
import dev.helight.hopper.ecs.craft.Bags
import dev.helight.hopper.ecs.craft.EcsMob
import dev.helight.hopper.ecs.impl.components.*
import dev.helight.hopper.ecs.system.HopperSystemTicker
import dev.helight.hopper.effects.Effects
import dev.helight.hopper.effects.Effects.cancelAfter
import dev.helight.hopper.extensions.PlayerExtensions.raycast
import dev.helight.hopper.extensions.VLBExtensions.destructible
import dev.helight.hopper.external.ColorApi
import dev.helight.hopper.inventory.v1.debug.ItemDataViewer
import dev.helight.hopper.puppetboy.EntityExtension.goalSelector
import dev.helight.hopper.puppetboy.EntityExtension.handle
import dev.helight.hopper.puppetboy.EntityExtension.puppet
import dev.helight.hopper.puppetboy.VanillaGoals
import dev.helight.hopper.puppetboy.goals.LocationSelector
import dev.helight.hopper.puppetboy.goals.impl.LookAtGoal
import dev.helight.hopper.utilities.Chat
import dev.helight.hopper.utilities.KVec.inverseTransform
import dev.helight.hopper.utilities.KVec.transformFast
import dev.helight.hopper.utilities.Raycast
import kotlinx.coroutines.launch
import net.minecraft.world.entity.EntityInsentient
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Cow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.util.Vector
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

    @Subcommand("debug puppetboy")
    @CommandPermission("hopper.debug.puppetboy")
    fun puppetBoy(player: Player) {
        val entity = player.world.spawn(player.location, Zombie::class.java)
        val handle = entity.handle
        val puppet = entity.puppet
        val goalSelector = entity.goalSelector
        goalSelector.clear()
        goalSelector.add(0, VanillaGoals.float(handle))
        goalSelector.add(1, LookAtGoal(puppet, LocationSelector.EntityLocation(player)))
    }

    @Subcommand("debug spawnPlayer")
    @CommandPermission("hopper.debug.spawnPlayer")
    fun spawnPlayer(player: Player) {
        hopper.spigot.proto.spawnPlayer(player)
    }

    @Subcommand("ticks")
    @CommandPermission("hopper.ticks")
    fun ticks(player: Player) {
        val runtime = Runtime.getRuntime()

        val message = combined(
            combined(
                TextComponents.chatPrefix("HopperSystem"),
                textComponent() with "Showing system timings and stats" with basicPreset with TextMod.NEWLINE,
            ),
            combined(
                textComponent() with "TPS: " with basicPreset,
                textComponent() with (HopperSystemTicker.tps.toString()) with strongPreset with TextMod.TAB
            ).padEnd(15),
            combined(
                textComponent() with "TLS: " with basicPreset,
                textComponent() with (HopperSystemTicker.ticksLastSecond.toString()) with strongPreset with TextMod.TAB
            ).padEnd(15),
            combined(
                textComponent() with "TLM: " with basicPreset,
                textComponent() with (HopperSystemTicker.ticksLastMinute.toString()) with strongPreset with TextMod.NEWLINE
            ),
            combined(
                textComponent() with "ECS Size: " with basicPreset,
                textComponent() with (ecs.storage.globalEntityList.size.toString()) with strongPreset with TextMod.TAB
            ).padEnd(15),
            combined(
                textComponent() with "ECS AM Size: " with basicPreset,
                textComponent() with (ecs.storage.mappedArchetypes.size.toString()) with strongPreset with TextMod.NEWLINE
            ),
            combined(
                textComponent() with "Delta Time: " with basicPreset,
                textComponent() with (HopperSystemTicker.deltaTime.toString()) with strongPreset with TextMod.NEWLINE
            ).padEnd(15),
            combined(
                textComponent() with "Free Memory: " with basicPreset,
                textComponent() with ((runtime.freeMemory() / 1048576).toString() + "MB") with strongPreset with TextMod.NEWLINE
            ),
        )

        player.spigot().sendMessage(message)
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


    @Subcommand("debug blobupload")
    @CommandPermission("hopper.debug.blobupload")
    fun blobUpload(player: Player, url: String, user: String, password: String, path: String, content: String) {
        offstageAsync {
            val repo = HttpBlobRepository.fromBasicAuth(url, user, password)
            repo.create(HttpBlobRepository.BlobEntity(path, content))
            MessageBuilder {
                chatPrefix("HttpBlobRepository")
                basic("Blob created")
            }
        }
    }

    @Subcommand("debug blobget")
    @CommandPermission("hopper.debug.blobget")
    fun blobGet(player: Player, url: String, user: String, password: String, path: String) {
        offstageAsync {
            val repo = HttpBlobRepository.fromBasicAuth(url, user, password)
            val content = repo.get(path).content
            MessageBuilder {
                chatPrefix("HttpBlobRepository")
                add(" ") basic (content)
            }.send(player)
        }
    }

    @Subcommand("debug reflections")
    fun simulateReflection(player: Player) {
        val t1 = Instant.now()
        Raycast.castRaycast(player.eyeLocation,
            player.eyeLocation.clone().add(player.eyeLocation.direction.normalize().multiply(1)),
            Raycast.RaycastOptions(
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
        hopper.spigot.spawnEntity(player.location, EntityType.ZOMBIE)
        /*
        val compose = File("entity.compose.json").readText()
        ecs.composer.executeLocational(compose, player.location)

         */
    }

    @Subcommand("debug entityx20")
    @CommandPermission("hopper.debug.entity")
    fun spawnEntity20(player: Player) {
        for (i in 0 until 20) {
            hopper.spigot.spawnEntity(player.location, EntityType.ZOMBIE)
        }
        /*
        val compose = File("entity.compose.json").readText()
        ecs.composer.executeLocational(compose, player.location)

         */
    }

    @Subcommand("debug goalTest")
    @CommandPermission("hopper.debug.goalTest")
    fun goalTest(player: Player) {
        val goalManager = Bukkit.getMobGoals()
        val mob = player.world.spawn(player.location, Cow::class.java)

        goalManager.getAllGoals(mob).forEach {
            println("${it.key}: ${it.javaClass.name}")
        }
        goalManager.removeAllGoals(mob)
        val ceClass = Class.forName("${MinecraftReflection.getCraftBukkitPackage()}.entity.CraftEntity")
        val craftEntity = ceClass.cast(mob)
        val handle = ceClass.getMethod("getHandle").invoke(craftEntity)
        val sentient = handle as EntityInsentient
        sentient.bR.a(0, PathfinderGoalFloat(handle))
        sentient.bR
    }

    @Subcommand("debug persistentEcs")
    @CommandPermission("hopper.debug.persistentEcs")
    fun debugPersistentEntity(player: Player) {
        ecs.create {
            add<DebugComponent>(DebugComponent("Persistent Entity ${UUID.randomUUID()}"))
        }
    }

    @Subcommand("debug item bag")
    @CommandPermission("hopper.debug.item")
    fun getItemBag(player: Player) {
        offstageAsync {
            val item = Item.builder(Material.SNOWBALL).name("§bHopperBall").delegate()
            hopper.spigot.createItem(item) {
                //add<DebugComponent>(DebugComponent(name = "HopperItem"))
                tag<RollingMetaStorage>()
                add<BagComponent>(Bags.newFromSize("Snowball Inventory", 2))
            }

            launch(HopperDispatchers.SYNC) {
                player.location.world!!.dropItem(player.location, item)
            }
        }
    }

    @Subcommand("debug item bow")
    @CommandPermission("hopper.debug.item")
    fun getItemBow(player: Player) {
        offstageAsync {
            val item = Item.builder(Material.BOW).name("§bHopperBall").delegate()
            hopper.spigot.createItem(item) {

            }

            launch(HopperDispatchers.SYNC) {
                player.location.world!!.dropItem(player.location, item)
            }
        }
    }

    @Subcommand("debug item maxHealth")
    @CommandPermission("hopper.debug.item")
    fun getItemMaxHealth(player: Player) {
        offstageAsync {
            val item = Item.builder(Material.DIAMOND_CHESTPLATE)
                .extLore()
                .name("§bMax Chestplate").delegate()
            hopper.spigot.createItem(item) {
                tag<ActiveWearable>()
                add<IncreaseMaxHealth>(IncreaseMaxHealth(20.0))
            }

            launch(HopperDispatchers.SYNC) {
                player.location.world!!.dropItem(player.location, item)
            }
        }
    }

    @Subcommand("debug item regen")
    @CommandPermission("hopper.debug.item")
    fun getItemRegen(player: Player) {
        offstageAsync {
            val item = Item.builder(Material.DIAMOND_HELMET)
                .extLore()
                .name("§bRegen Helmet").delegate()
            hopper.spigot.createItem(item) {
                tag<ActiveWearable>()
                add<IncreaseRegenRate>(IncreaseRegenRate(1.0))
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
        val entity = EcsMob.getNearbyEntities(player.raycast(), 0.5).first()
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