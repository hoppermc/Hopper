package dev.helight.hopper

import co.aikar.commands.PaperCommandManager
import de.slikey.effectlib.EffectManager
import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.commands.HopperEngineCommand
import dev.helight.hopper.ecs.BufferedEntity
import dev.helight.hopper.ecs.craft.*
import dev.helight.hopper.ecs.craft.listeners.EntityEngineListener
import dev.helight.hopper.ecs.craft.listeners.HealthEngineListener
import dev.helight.hopper.ecs.craft.listeners.ItemEngineListener
import dev.helight.hopper.ecs.craft.listeners.PlayerEngineListener
import dev.helight.hopper.ecs.craft.systems.EcsEntitySystem
import dev.helight.hopper.ecs.craft.systems.HopperHealthMobSystem
import dev.helight.hopper.ecs.craft.systems.HopperHealthPlayerSystem
import dev.helight.hopper.ecs.craft.systems.HopperRegenSystem
import dev.helight.hopper.ecs.impl.DebugComponentSystem
import dev.helight.hopper.ecs.impl.DebugEventHandler
import dev.helight.hopper.ecs.impl.components.*
import dev.helight.hopper.ecs.impl.jobs.ItemJob
import dev.helight.hopper.ecs.impl.jobs.MaxHealthReevaluationJob
import dev.helight.hopper.ecs.impl.jobs.RegenReevaluationJob
import dev.helight.hopper.ecs.impl.serializers.*
import dev.helight.hopper.extensions.EntityExtensions.living
import dev.helight.hopper.inventory.v1.GuiEventListener
import dev.helight.hopper.inventory.v1.GuiGarbageCollector
import dev.helight.hopper.puppetboy.PuppetBoy
import dev.helight.hopper.utilities.Persistence.store
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.quartz.JobBuilder.newJob
import java.io.File
import java.net.URI
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.set

@Suppress("EXPERIMENTAL_API_USAGE")
class HopperSpigot {
    lateinit var commandManager: PaperCommandManager
    lateinit var effectManager: EffectManager
    lateinit var proto: ProtoLibHook
    lateinit var puppetBoy: PuppetBoy

    internal fun hookSerializers() {
        ecs.registerDefaultSerializerForClass<TransientEntity>()
        ecs.registerDefaultSerializerForClass<RollingMetaStorage>()
        ecs.registerDefaultSerializerForClass<ActiveWearable>()

        ecs.serializer<EcsMob>(EntitySerializer())
        ecs.serializer<EcsItem>(SpigotItemSerializer())
        ecs.serializer<EcsPlayer>(SpigotPlayerSerializer())
        ecs.serializer<DebugComponent>(DebugComponentSerializer())
        ecs.serializer<BagComponent>(BagComponentSerializer())
        ecs.serializer<HopperDamage>(HopperDamageSerializer())
        ecs.serializer<HopperHealth>(HopperHealthSerializer())
        ecs.serializer<HopperRegen>(HopperRegenSerializer())
        ecs.serializer<IncreaseMaxHealth>(IncreaseMaxHealthSerializer())
        ecs.serializer<IncreaseRegenRate>(IncreaseRegenRateSerializer())
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

        puppetBoy = PuppetBoy(HopperSpigotHook.plugin)

        Bukkit.getScheduler().runTaskTimer(HopperSpigotHook.plugin, Runnable {
            Bukkit.getWorlds().forEach {
                val before = EcsMob.globalEntityCache[it.name]
                val after = it.entities
                EcsMob.globalEntityCache[it.name] = after
                if (before == null) {
                    after.forEach(::checkLoadEntity)
                } else {
                    after.filterNot { entity -> before.contains(entity) }.forEach(::checkLoadEntity)
                }
            }
        }, 0, 1)

        setupGui()

        BetterListener.assureRegistered(EntityEngineListener::class.java)
        BetterListener.assureRegistered(ItemEngineListener::class.java)
        BetterListener.assureRegistered(PlayerEngineListener::class.java)
        BetterListener.assureRegistered(HealthEngineListener::class.java)

        ecs.system<EcsEntitySystem>()
        ecs.system<DebugComponentSystem>()
        ecs.system<HopperHealthPlayerSystem>()
        ecs.system<HopperHealthMobSystem>()
        ecs.system<HopperRegenSystem>()

        ecs.handler<DebugEventHandler>()
        ecs.handler<IncreaseRegenReevaluateHandler>()
        ecs.handler<IncreaseMaxHealthReevaluateHandler>()
        ecs.handler<ExtendHealthItemInfo>()

        ecs.directEvents.register(BagHandler())

        hopper.schedule(newJob(ItemJob::class.java)
            .withIdentity("itemJob", "hopper")
            .build(), hopper.getPerSecondTrigger("itemJobTrigger", "hopper"))

        hopper.schedule(newJob(MaxHealthReevaluationJob::class.java)
            .withIdentity("maxHealthReevaluateJob", "hopper")
            .build(), hopper.getPerSecondTrigger("maxHealthReevaluateJobTrigger", "hopper"))

        hopper.schedule(newJob(RegenReevaluationJob::class.java)
            .withIdentity("regenReevaluateJob", "hopper")
            .build(), hopper.getPerSecondTrigger("regenReevaluateJobTrigger", "hopper"))


        commandManager.registerCommand(HopperEngineCommand())
    }

    private fun checkLoadEntity(entity: Entity) = try {
        val hopperID = EcsMob.getHopper(entity)
        if (hopperID != null) {
            println("Confirmed as hopper entity ${entity.entityId}. Loading.")
            EcsMob.load(entity)
        } else {}
    } catch (ex: Exception) {
        ex.printStackTrace()
    }


    internal fun unhook() {
        ecs.query(EcsMob::class.java).forEach {
            val se = it.get<EcsMob>()
            val entity = runBlocking { se.resolve() }
            if (entity != null) EcsMob.store(entity, it.entityId)
        }

        val items = ecs.query(EcsItem::class.java)
        val latch = CountDownLatch(items.size)
        items.forEach {
            val si = it.get<EcsItem>()
            println(si)
            val item = si.getHolder()?.inventory?.first { item ->
                val hopper = EcsItem.getHopper(item)
                hopper != null && hopper == it.entityId
            }
            when(item) {
                null -> {
                    latch.countDown()
                    println("Item is not in inventory of assigned holder => Skipping store")
                }
                else -> offstageAsync {
                    EcsItem.store(item, it.entityId)
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

    fun spawnEntity(location: Location, type: EntityType, block: BufferedEntity.() -> Unit = {}): EntityId {
        val hopperId = ecs.newEntityId()

        synchronizeDecoupled {
            val spigot = location.world!!.spawnEntity(location, type)
            spigot.persistentDataContainer.store("HopperSpigotEntity", hopperId.toString())
            val mob = EcsMob.forEntity(spigot)
            val living = spigot.living!!

            val buffer = BufferedEntity()
            buffer.entity = buffer.entity.copy(first = hopperId)
            buffer.tag<TransientEntity>()
            buffer.add<EcsMob>(mob)
            buffer.add<HopperHealth>(HopperHealth(living.health, living.maxHealth, living.maxHealth))
            block(buffer)
            ecs.push(buffer.entity)

            spigot.isPersistent = true
            spigot.living?.removeWhenFarAway = false
        }

        return hopperId
    }

    suspend fun createItem(item: ItemStack, block: suspend BufferedEntity.() -> Unit) = coroutineScope {
        val si = EcsItem(UUID.randomUUID().toString(), null)
        val entity = ecs.createSuspended {
            tag<TransientEntity>()
            add<EcsItem>(si)
            block(this)
        }

        println(entity.toString())
        EcsItem.setup(item, si, entity.first)
        EcsItem.store(item, entity.first)
    }

    suspend inline fun <reified T> addComponentToItem(item: ItemStack, data: ComponentData) {
        val hopper = EcsItem.getHopper(item) ?: error("Not an hopper item")
        if (ecs.storage.containsEntity(hopper)) {
            ecs.add<T>(hopper, data)
        } else {
            EcsItem.load(item)!!
            ecs.add<T>(hopper, data)
            EcsItem.store(item, hopper)
        }
    }

    companion object {
        private val incrementalCustomEntityId = AtomicInteger(Int.MIN_VALUE)
        fun getCustomEntityId(): Int = incrementalCustomEntityId.getAndIncrement()
    }

}

