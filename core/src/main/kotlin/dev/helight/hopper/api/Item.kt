package dev.helight.hopper.api

import com.google.common.collect.ForwardingObject
import com.google.gson.Gson
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import dev.helight.hopper.HopperSpigotHook
import net.minecraft.server.v1_16_R3.NBTTagCompound
import net.minecraft.server.v1_16_R3.NBTTagList
import net.minecraft.server.v1_16_R3.NBTTagString
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.IllegalArgumentException
import java.lang.reflect.Field
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

class Item : ForwardingObject {

    private var itemStack: ItemStack?

    constructor(itemStack: ItemStack?) {
        this.itemStack = itemStack
    }

    constructor(material: Material?) {
        itemStack = ItemStack(material!!, 1)
    }

    //Recommended Constructor for Kotlin Projects
    constructor(material: Material, amount: Int = 1, name: String? = null, vararg lore: String,
                flags: Collection<ItemFlag>? = null, enchantments: Collection<Pair<Enchantment,Int>>? = null,

                ) {
        itemStack = ItemStack(material, amount)
        if (name != null) name(name)
        if (lore.isNotEmpty()) lore(lore.toList())
        flags?.forEach { flag(it) }
        enchantments?.forEach {
            enchant(it.first, it.second)
        }
    }

    fun amount(amount: Int): Item {
        itemStack!!.amount = amount
        return this
    }

    fun name(name: String): Item {
        this.changeMeta { itemMeta: ItemMeta -> itemMeta.setDisplayName(name) }
        return this
    }

    fun lore(vararg strings: String): Item {
        this.changeMeta { itemMeta: ItemMeta -> itemMeta.lore = Arrays.asList(*strings) }
        return this
    }

    fun lore(strings: Collection<String>): Item {
        this.changeMeta { itemMeta: ItemMeta -> itemMeta.lore = ArrayList(strings) }
        return this
    }

    fun enchant(enchantment: Enchantment, level: Int): Item {
        itemStack!!.addUnsafeEnchantment(enchantment, level)
        return this
    }

    fun flag(itemFlag: ItemFlag): Item {
        changeMeta {
            it.addItemFlags(itemFlag)
        }
        return this
    }

    fun baseDamage(damage: Double): Item {
        changeMeta { meta: ItemMeta ->
            meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_DAMAGE,
                AttributeModifier("generic.attack_damage",
                    Math.max(damage - 1, 0.0),
                    AttributeModifier.Operation.ADD_NUMBER)
            )
        }
        return this
    }

    fun armor(damage: Double): Item {
        changeMeta { meta: ItemMeta ->
            meta.addAttributeModifier(
                Attribute.GENERIC_ARMOR,
                AttributeModifier("generic.armor", Math.max(damage - 1, 0.0), AttributeModifier.Operation.ADD_NUMBER)
            )
        }
        return this
    }

    fun canPlaceOn(material: Material): Item {
        editNMS { compound: NBTTagCompound ->
            val attributes: NBTTagList = compound.getList("CanPlaceOn", 8)
            attributes.add(NBTTagString.a("minecraft:" + material.name.toLowerCase()))
            compound.set("CanPlaceOn", attributes)
        }
        return this
    }

    fun canDestroy(material: Material?): Item {
        editNMS { compound: NBTTagCompound ->
            val attributes: NBTTagList = compound.getList("CanDestroy", 8)
            attributes.add(NBTTagString.a("minecraft:" + material!!.name.toLowerCase()))
            compound.set("CanPlaceOn", attributes)
        }
        return this
    }

    fun storeJson(o: Any): Item {
        val gson = Gson()
        val outputStream = ByteArrayOutputStream()
        val writer = OutputStreamWriter(outputStream)
        gson.toJson(o, writer)
        changeMeta { itemMeta: ItemMeta ->
            itemMeta.persistentDataContainer.set(NamespacedKey(HopperSpigotHook.plugin,
                o.javaClass.name), PersistentDataType.BYTE_ARRAY, outputStream.toByteArray())
        }
        return this
    }

    fun storeJson(key: String, o: Any): Item {
        val gson = Gson()
        val outputStream = ByteArrayOutputStream()
        val writer = OutputStreamWriter(outputStream)
        gson.toJson(o, writer)
        changeMeta { itemMeta: ItemMeta ->
            itemMeta.persistentDataContainer.set(NamespacedKey(HopperSpigotHook.plugin,
                key), PersistentDataType.BYTE_ARRAY, outputStream.toByteArray())
        }
        return this
    }

    fun <K> loadJson(clazz: Class<K>): K {
        val gson = Gson()
        val array = itemStack!!.itemMeta!!.persistentDataContainer.get(NamespacedKey(HopperSpigotHook.plugin, clazz.name),
            PersistentDataType.BYTE_ARRAY)
        val inputStream = ByteArrayInputStream(array)
        val reader = InputStreamReader(inputStream)
        return gson.fromJson(reader, clazz)
    }


    fun <K> loadJson(key: String, clazz: Class<K>): K {
        val gson = Gson()
        val array = itemStack!!.itemMeta!!.persistentDataContainer.get(NamespacedKey(HopperSpigotHook.plugin, key),
            PersistentDataType.BYTE_ARRAY)
        val inputStream = ByteArrayInputStream(array)
        val reader = InputStreamReader(inputStream)
        return gson.fromJson(reader, clazz)
    }

    fun load(key: String): String? {
        return itemStack!!.itemMeta!!.persistentDataContainer.get(NamespacedKey(HopperSpigotHook.plugin, key),
            PersistentDataType.STRING)
    }

    fun editNMS(function: Consumer<NBTTagCompound>): Item {
        val stack: net.minecraft.server.v1_16_R3.ItemStack = CraftItemStack.asNMSCopy(itemStack)
        val compound: NBTTagCompound = stack.orCreateTag
        function.accept(compound)
        stack.tag = compound
        itemStack = CraftItemStack.asCraftMirror(stack)
        return this
    }

    fun changeMetaFun(function: Function<ItemMeta, ItemMeta>): Item {
        var itemMeta = itemStack!!.itemMeta!!
        itemMeta = function.apply(itemMeta)
        itemStack!!.setItemMeta(itemMeta)
        return this
    }

    fun changeMeta(function: Consumer<ItemMeta>): Item {
        val itemMeta = itemStack!!.itemMeta!!
        function.accept(itemMeta)
        itemStack!!.setItemMeta(itemMeta)
        return this
    }

    public override fun delegate(): ItemStack {
        return itemStack ?: ItemStack(Material.AIR)
    }

    companion object {
        fun builder(material: Material): Item {
            return Item(material)
        }

        fun builder(itemStack: ItemStack?): Item {
            return Item(itemStack)
        }

        fun remove(player: Player, itemStack: ItemStack, amount: Int): Boolean {
            return if (player.inventory.contains(itemStack.clone().apply {
                    this.amount = 1
                }, amount)) {
                player.inventory.remove(itemStack.clone().apply {
                    this.amount = amount
                })
                true
            } else {
                false
            }
        }

        fun add(player: Player, itemStack: ItemStack?) {
            player.inventory.addItem(itemStack!!).forEach { (integer: Int?, itemStack1: ItemStack?) ->
                player.location.world!!.dropItem(player.location.add(0.0,
                    0.5,
                    0.0), itemStack)
            }
        }

        fun getCustomTextureHead(value: String?): ItemStack {
            val head = ItemStack(Material.PLAYER_HEAD, 1)
            val meta = head.itemMeta as SkullMeta
            val profile = GameProfile(UUID.randomUUID(), "")
            profile.getProperties().put("textures", Property("textures", value))
            var profileField: Field? = null
            try {
                profileField = meta.javaClass.getDeclaredField("profile")
                profileField.isAccessible = true
                profileField[meta] = profile
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            head.setItemMeta(meta)
            return head
        }

        fun fromHead(value: String?): Item {
            val head = ItemStack(Material.PLAYER_HEAD, 1)
            val meta = head.itemMeta as SkullMeta
            val profile = GameProfile(UUID.randomUUID(), "")
            profile.getProperties().put("textures", Property("textures", value))
            var profileField: Field? = null
            try {
                profileField = meta.javaClass.getDeclaredField("profile")
                profileField.isAccessible = true
                profileField[meta] = profile
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            head.setItemMeta(meta)
            return builder(head)
        }
    }
}