package dev.helight.hopper.api

import com.google.gson.Gson
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*
import java.util.function.Consumer

class SerializedItem {
    private val name: String? = null
    private val lore: List<String> = ArrayList()
    private val model = 0
    private val material: String? = null
    private val amount = 1
    private val enchantments: List<SerializedEnchantment> = ArrayList()
    private val attributes: List<SerializedAttribute> = ArrayList()
    private val canPlaceOn: List<String> = ArrayList()
    private val canDestroy: List<String> = ArrayList()

    class SerializedEnchantment {
        val name: String? = null
        val level = 1
    }

    class SerializedAttribute {
        val name: String? = null
        val value: Double? = null
        val operation = "ADD"
    }

    fun build(): ItemStack? {
        val item: Item = Item.builder(Material.matchMaterial(
            material!!)!!)
            .amount(amount)
            .name(name!!)
            .lore(lore)
        item.changeMeta { meta: ItemMeta -> meta.setCustomModelData(0) }
        for (s in canDestroy) item.canDestroy(Material.matchMaterial(s))
        for (s in canDestroy) item.canPlaceOn(Material.matchMaterial(s)!!)
        for (enchantment in enchantments) {
            val bukkit = Arrays.stream(Enchantment.values())
                .filter { query: Enchantment ->
                    query.key.key.endsWith(
                        enchantment.name!!.toLowerCase())
                }
                .findFirst().orElse(Enchantment.LUCK)
            item.enchant(bukkit, enchantment.level)
        }
        for (attribute in attributes) {
            item.changeMeta { meta: ItemMeta ->
                meta.addAttributeModifier(
                    Attribute.valueOf(attribute.name!!.replace("\\.".toRegex(), "_").toUpperCase()),
                    AttributeModifier(attribute.name,
                        attribute.value!!,
                        if (attribute.operation.toLowerCase() == "add") AttributeModifier.Operation.ADD_NUMBER else AttributeModifier.Operation.ADD_SCALAR)
                )
            }
        }
        return item.delegate()
    }

    companion object {
        fun deserialize(content: String?): ItemStack? {
            val gson = Gson()
            val item = gson.fromJson(content, SerializedItem::class.java)
            return item.build()
        }
    }
}