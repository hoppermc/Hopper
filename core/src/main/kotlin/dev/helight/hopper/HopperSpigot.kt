package dev.helight.hopper

import co.aikar.commands.PaperCommandManager
import de.slikey.effectlib.EffectManager
import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.commands.HopperEngineCommand
import dev.helight.hopper.ecs.BufferedEntity
import dev.helight.hopper.ecs.ComponentSerializer
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.craft.*
import dev.helight.hopper.ecs.event.Event
import dev.helight.hopper.ecs.event.HopperEvent
import dev.helight.hopper.ecs.event.HopperEventHandler
import dev.helight.hopper.ecs.system.HopperSystem
import dev.helight.hopper.entity.SpigotEntity
import dev.helight.hopper.entity.SpigotEntitySystem
import dev.helight.hopper.extensions.EntityExtensions.living
import dev.helight.hopper.inventory.v1.GuiEventListener
import dev.helight.hopper.inventory.v1.GuiGarbageCollector
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent
import org.bukkit.inventory.ItemStack
import org.quartz.JobBuilder.newJob
import java.io.File
import java.net.URI
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

@Suppress("EXPERIMENTAL_API_USAGE")
class HopperSpigot {


    lateinit var commandManager: PaperCommandManager
    lateinit var effectManager: EffectManager
    lateinit var proto: ProtoLibHook

    internal fun hookSerializers() {
        ecs.serializer<TransientEntity>(TransientComponentSerializer())
        ecs.serializer<SpigotEntity>(EntitySerializer())
        ecs.serializer<SpigotItem>(SpigotItemSerializer())
        ecs.serializer<DebugComponent>(DebugComponentSerializer())
    }

    fun isProtocolLibDownloaded(): Boolean = try {
        Class.forName("com.comphenix.protocol.ProtocolLibrary")
        true
    } catch (ex: Exception) {
        false
    }

    internal fun hook() {

        if (!isProtocolLibDownloaded()) {
            println("Protocol Lib is not loaded")
            val file = File("plugins", "ProtocolLib.jar")
            val httpStream =
                URI.create("https://github.com/dmulloy2/ProtocolLib/releases/download/4.7.0/ProtocolLib.jar").toURL().openStream()
            val fileStream = file.outputStream()
            httpStream.copyTo(fileStream)
            httpStream.close()
            fileStream.close()
            Bukkit.getPluginManager().loadPlugin(file)
            if (isProtocolLibDownloaded()) proto = ProtoLibHook()
            else error("Protocol Lib could not be loaded")
        } else {
            proto = ProtoLibHook()
            println("Protocol Lib is loaded")
        }

        commandManager = PaperCommandManager(HopperSpigotHook.plugin)
        effectManager = EffectManager(HopperSpigotHook.plugin)

        Bukkit.getScheduler().runTaskTimer(HopperSpigotHook.plugin, Runnable {
            Bukkit.getWorlds().forEach {
                SpigotEntity.globalEntityCache[it.name] = it.entities
            }
        }, 0, 1)

        setupGui()

        BetterListener.assureRegistered(EntityEngineListener::class.java)
        BetterListener.assureRegistered(ItemEngineListener::class.java)

        ecs.system<SpigotEntitySystem>()
        ecs.system<DebugComponentSystem>()

        ecs.handler<DebugEventHandler>()

        hopper.schedule(newJob(ItemJob::class.java)
            .withIdentity("itemJob", "hopper")
            .build(), hopper.getPerSecondTrigger("itemJobTrigger", "hopper"))

        commandManager.registerCommand(HopperEngineCommand())
    }

    internal fun unhook() {
        ecs.query(SpigotEntity::class.java).forEach {
            val se = it.get<SpigotEntity>()
            val entity = se.resolve()
            if (entity != null) SpigotEntity.store(entity, it.entityId)
        }

        val items = ecs.query(SpigotItem::class.java)
        val latch = CountDownLatch(items.size)
        items.forEach {
            val si = it.get<SpigotItem>()
            println(si)
            val item = si.getHolder()?.inventory?.first { item ->
                val hopper = SpigotItem.getHopper(item)
                hopper != null && hopper == it.entityId
            }
            when(item) {
                null -> {
                    latch.countDown()
                    println("Item is not in inventory of assigned holder => Skipping store")
                }
                else -> offstageAsync {
                    SpigotItem.store(item, it.entityId)
                    latch.countDown()
                }
            }
        }
        latch.await()
    }


    private fun setupGui() {
        BetterListener.assureRegistered(GuiEventListener::class.java)
        hopper.schedule(
            newJob(GuiGarbageCollector::class.java)
                .withIdentity("guiGarbageCollector", "hopper")
                .build(), hopper.getPerSecondTrigger("garbageCollectorTickTrigger", "hopper")
        )
    }


    fun spawnEntity(location: Location, type: EntityType): EntityId {
        val spigot = location.world!!.spawnEntity(location, type)
        val se = SpigotEntity.forEntity(spigot)
        val hopperId = ecs.createEntityWithOperation()
        ecs.add<SpigotEntity>(hopperId, se)
        spigot.isPersistent = true
        spigot.living?.removeWhenFarAway = false

        SpigotEntity.setup(spigot, se, hopperId)

        return hopperId
    }

    suspend fun createItem(item: ItemStack, block: suspend BufferedEntity.() -> Unit) = coroutineScope {
        val si = SpigotItem(UUID.randomUUID().toString(), null)
        val entity = ecs.createSuspended {
            tag<TransientEntity>()
            add<SpigotItem>(si)
            block(this)
        }

        println(entity.toString())
        SpigotItem.setup(item, si, entity.first)
        SpigotItem.store(item, entity.first)
    }

    suspend inline fun <reified T> addComponentToItem(item: ItemStack, data: ComponentData) {
        val hopper = SpigotItem.getHopper(item) ?: error("Not an hopper item")
        if (ecs.storage.containsEntity(hopper)) {
            ecs.add<T>(hopper, data)
        } else {
            SpigotItem.load(item)!!
            ecs.add<T>(hopper, data)
            SpigotItem.store(item, hopper)
        }
    }

    companion object {
        private val incrementalCustomEntityId = AtomicInteger(Int.MIN_VALUE)
        fun getCustomEntityId(): Int = incrementalCustomEntityId.getAndIncrement()
    }

}

@Serializable
@SerialName("hopper:debug")
data class DebugComponent(
    val name: String
)

@ExperimentalUnsignedTypes
class DebugEventHandler : HopperEventHandler(HopperEvent::class) {
    override suspend fun handle(event: ExportedEntityWrapper) {
        println("Event '${event.data.first { it.second is Event }.second?.javaClass?.simpleName ?: "null"}' [${event.entityId}]")
    }

}

@ExperimentalUnsignedTypes
class DebugComponentSystem : HopperSystem(DebugComponent::class) {

    override fun tickIndividual(entity: ExportedEntityWrapper) {
        println(entity.toString())
        val debug = entity.get<DebugComponent>()
        println("Ticking '${debug.name}' [${entity.entityId}]")
    }

}

class HopperEntityCreateEvent(val delegate: Entity, val hopperEntity: SpigotEntity) : EntityEvent(delegate) {
    override fun getHandlers(): HandlerList = handlers

    companion object {
        val handlers = HandlerList()
    }
}

class HopperEntitySpawnEvent(val delegate: Entity, val hopperEntity: SpigotEntity) : EntityEvent(delegate) {
    override fun getHandlers(): HandlerList = handlers

    companion object {
        val handlers = HandlerList()
    }
}

class HopperEntityDespawnEvent(val delegate: Entity, val hopperEntity: SpigotEntity) : EntityEvent(delegate) {
    override fun getHandlers(): HandlerList = handlers

    companion object {
        val handlers = HandlerList()

    }
}

class HopperEntityDestroyEvent(val delegate: Entity, val hopperEntity: SpigotEntity) : EntityEvent(delegate) {
    override fun getHandlers(): HandlerList = handlers

    companion object {
        val handlers = HandlerList()
    }
}

class EntitySerializer: ComponentSerializer {
    override fun serialize(value: Any?): String {
        return Json.encodeToString(value as SpigotEntity)
    }

    override fun deserialize(data: String): Any {
        return Json.decodeFromString<SpigotEntity>(data)
    }
}

class DebugComponentSerializer: ComponentSerializer {
    override fun serialize(value: Any?): String {
        return Json.encodeToString(value as DebugComponent)
    }

    override fun deserialize(data: String): Any {
        return Json.decodeFromString<DebugComponent>(data)
    }
}