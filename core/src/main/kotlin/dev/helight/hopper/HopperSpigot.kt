package dev.helight.hopper

import co.aikar.commands.PaperCommandManager
import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.commands.HopperEngineCommand
import dev.helight.hopper.ecs.ComponentSerializer
import dev.helight.hopper.ecs.ExportedEntityWrapper
import dev.helight.hopper.ecs.HopperSystem
import dev.helight.hopper.ecs.HopperSystemOptions
import dev.helight.hopper.entity.EntityEngineListener
import dev.helight.hopper.entity.SpigotEntity
import dev.helight.hopper.entity.SpigotEntitySystem
import dev.helight.hopper.extensions.EntityExtensions.living
import dev.helight.hopper.inventory.GuiEventListener
import dev.helight.hopper.inventory.GuiGarbageCollector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent
import org.bukkit.inventory.ItemStack
import org.quartz.JobBuilder
import org.quartz.JobBuilder.newJob
import java.util.*

@ExperimentalUnsignedTypes
class HopperSpigot {

    lateinit var commandManager: PaperCommandManager

    internal fun hookSerializers() {
        ecs.serializer<SpigotEntity>(EntitySerializer())
        ecs.serializer<DebugComponent>(DebugComponentSerializer())
    }

    internal fun hook() {
        commandManager = PaperCommandManager(HopperSpigotHook.plugin)
        setupGui()

        BetterListener.assureRegistered(EntityEngineListener::class.java)
        BetterListener.assureRegistered(ItemEngineListener::class.java)

        ecs.system<SpigotEntitySystem>()
        ecs.system<DebugComponentSystem>()

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
    }

    private fun setupGui() {
        BetterListener.assureRegistered(GuiEventListener::class.java)
        hopper.schedule(
            JobBuilder.newJob(GuiGarbageCollector::class.java)
                .withIdentity("guiGarbageCollector", "hopper")
                .build(), hopper.getPerSecondTrigger("garbageCollectorTickTrigger", "hopper")
        )
    }


    fun spawnEntity(location: Location, type: EntityType): EntityId {
        val spigot = location.world!!.spawnEntity(location, type)
        val se = SpigotEntity.forEntity(spigot)
        val hopperId = ecs.createEntity()
        ecs.add<SpigotEntity>(hopperId, se)
        spigot.isPersistent = true
        spigot.living?.removeWhenFarAway = false

        SpigotEntity.setup(spigot, se, hopperId)

        return hopperId
    }

    fun convertToHopper(item: ItemStack) {
        val si = SpigotItem(UUID.randomUUID().toString(), null)
        val hopperId = ecs.createEntity()
        ecs.add<SpigotItem>(hopperId, si)
        SpigotItem.setup(item, si, hopperId)
        SpigotItem.store(item, hopperId)
    }

    inline fun <reified T> addComponentToItem(item: ItemStack, data: ComponentData) {
        val hopper = SpigotItem.getHopper(item) ?: error("Not an hopper item")
        if (ecs.storage.containsEntity(hopper)) {
            ecs.add<T>(hopper, data)
        } else {
            println("TransferLoading Item")
            SpigotItem.load(item)!!
            println("Loaded Transfer Item")
            ecs.add<T>(hopper, data)
            SpigotItem.store(item, hopper)
            println("Stored Transfer Item")
        }
    }

}

@Serializable
@SerialName("hopper:debug")
data class DebugComponent(
    val name: String
)

class DebugComponentSystem : HopperSystem(sortedSetOf(DebugComponent::class.java.toKey()), HopperSystemOptions(isTicking = true)) {

    override fun tickIndividual(entity: ExportedEntityWrapper) {
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