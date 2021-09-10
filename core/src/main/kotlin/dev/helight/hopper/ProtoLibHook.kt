package dev.helight.hopper

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftReflection
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject
import dev.helight.hopper.utilities.Chat.toChatComponentText
import dev.helight.hopper.utilities.Constants
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*


class ProtoLibHook {

    val manager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    init {
        println("=================================")
        println("PROTOCOL LIB HAS BEEN HOOKED")
        println("The Hopper Engine can now use")
        println("packet more packet stuff")
        println()
        println("ProtocolLib Detected Version:")
        println(manager.minecraftVersion)
        println(MinecraftReflection.getMinecraftPackage())
        println(MinecraftReflection.getCraftBukkitPackage())
        println("=================================")
    }

    fun unsafeCamSwitch(player: Player, entity: Entity) {
        val container = PacketContainer(PacketType.Play.Server.CAMERA)
        container.integers.write(0, entity.entityId)
        manager.sendServerPacket(player, container)
    }

    fun playExplosion(player: Player, location: Location, strength: Float = 3f) {
        val container = PacketContainer(PacketType.Play.Server.EXPLOSION)
        container.doubles
            .write(0, location.x)
            .write(1, location.y)
            .write(2, location.z)
        container.float
            .write(0, strength)
        container.intLists
            .write(0, listOf())
        manager.sendServerPacket(player, container)
    }

    fun spawnWorldString(player: Player, location: Location, title: String): Int {
        val uuid = UUID.randomUUID()
        val container = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING)
        val id = HopperSpigot.getCustomEntityId()
        container.uuiDs
            .write(0, uuid)
        container.integers
            .write(0, id)
            .write(1, 1)
            .write(2, 0)
            .write(3, 0)
            .write(4, 0)
        container.doubles
            .write(0, location.x)
            .write(1, location.y - Constants.NAMETAG_HEIGHT)
            .write(2, location.z)
        container.bytes
            .write(0, 0)
            .write(1, 0)
            .write(2, 0)

        manager.sendServerPacket(player, container)

        val handle2 = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
        handle2.modifier.writeDefaults()
        handle2.integers.write(0, id)

        val dataWatcher = WrappedDataWatcher(handle2.watchableCollectionModifier.read(0))

        val isInvisibleIndex = WrappedDataWatcherObject(
            0, WrappedDataWatcher.Registry.get(
                Class.forName("java.lang.Byte")
            )
        )

       dataWatcher.setObject(isInvisibleIndex, 0x20.toByte())

        val isSmall = WrappedDataWatcherObject(
            14 /* 15 in 1.17 */, WrappedDataWatcher.Registry.get(
                Class.forName("java.lang.Byte")
            )
        )

        dataWatcher.setObject(isSmall, (0x01 or 0x10).toByte())

        val nameValue = WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true))

        val nameVisible = WrappedDataWatcherObject(
            3, WrappedDataWatcher.Registry.get(
                Class.forName("java.lang.Boolean")
            )
        )

        dataWatcher.setObject(nameValue, Optional.of(title.toChatComponentText()))
        dataWatcher.setObject(nameVisible, true)

        handle2.watchableCollectionModifier.write(0, dataWatcher.watchableObjects)
        manager.sendServerPacket(player, handle2)

        return id
    }

    fun destroyEntity(player: Player, entityId: Int) {
        val container = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)
        container.integerArrays
            .write(0, arrayOf(entityId).toIntArray())
        manager.sendServerPacket(player, container)
    }

    fun destroyEntity(player: Player, vararg entityIds: Int) {
        val container = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)
        container.integerArrays
            .write(0, entityIds)
        manager.sendServerPacket(player, container)
    }

}