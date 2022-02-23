package dev.helight.hopper

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftReflection
import com.comphenix.protocol.wrappers.*
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
        println("more packet stuff")
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

    fun spawnPlayer(player: Player) {
        val container1 = PacketContainer(PacketType.Play.Server.PLAYER_INFO)
        container1.playerInfoAction.write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER)
        container1.playerInfoDataLists.write(0, listOf(PlayerInfoData(
            WrappedGameProfile(UUID.fromString("7db383f0-b2d5-4b15-8bcf-0e1076d6c394"), "HelightBot"),
            1, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText("Bot")
        )))
        manager.sendServerPacket(player, container1)
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
            .write(0, uuid) // UUID
        container.integers
            .write(0, id) // Entity ID
            .write(1, 1) // Entity Type

            .write(2, 0) // Yaw
            .write(3, 0) // Pitch
            .write(4, 0) // Head Pitch
        container.doubles
            .write(0, location.x) // X
            .write(1, location.y - Constants.NAMETAG_HEIGHT) // Y
            .write(2, location.z) // Z
        container.bytes
            .write(0, 0) // Velocity X
            .write(1, 0) // Velocity Y
            .write(2, 0) // Velocity Z


        manager.sendServerPacket(player, container)

        val handle2 = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)

        handle2.modifier.writeDefaults()
        handle2.integers.write(0, id)

        val byteSerializer = WrappedDataWatcher.Registry.get(Class.forName("java.lang.Byte"))
        val booleanSerializer = WrappedDataWatcher.Registry.get(Class.forName("java.lang.Boolean"))
        val optionalChatSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true)

        val dataWatcher = WrappedDataWatcher(handle2.watchableCollectionModifier.read(0)) //TODO: Fix this fucking call (null iterator)

        val isInvisibleIndex = WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer)

        dataWatcher.setObject(isInvisibleIndex, 0x20.toByte())

        val isSmall = WrappedDataWatcher.WrappedDataWatcherObject(15 /* 15 in 1.17 */, byteSerializer)

        dataWatcher.setObject(isSmall, (0x01 or 0x10).toByte())

        val nameValue = WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true))

        val nameVisible = WrappedDataWatcher.WrappedDataWatcherObject(3, booleanSerializer)

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