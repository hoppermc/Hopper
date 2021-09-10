package dev.helight.hopper.ecs.data

import dev.helight.hopper.api.Item
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.*

@Serializable
@SerialName("mc:item:properties")
data class ItemProperties(
    val material: String,
    val amount: Int = 1,
    val name: String?,
    val lore: List<String>?,
    val enchantments: Map<String, Int> = mutableMapOf(),
    val indestructible: Boolean = false
) {
    fun construct(): ItemStack {
        val item: Item = Item.builder(Material.matchMaterial(material)!!).amount(amount)
        if (name != null) item.name(name)
        if (lore != null) item.lore(lore)
        item.changeMeta { meta: ItemMeta -> meta.setCustomModelData(0) }
        for (enchantment in enchantments) {
            val bukkit = Arrays.stream(Enchantment.values()).filter { query: Enchantment -> query.key.key.lowercase().endsWith(enchantment.key.toLowerCase()) }
                .findFirst().orElse(Enchantment.LUCK)
            item.enchant(bukkit, enchantment.value)
        }
        if (indestructible) item.changeMeta { it.isUnbreakable = true }
        return item.delegate()
    }

    companion object {
        fun parseFormString(content: String): ItemProperties = Json.decodeFromString(content)
    }
}