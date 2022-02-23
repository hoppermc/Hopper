package dev.helight.hopper.inventory.v1.debug

import dev.helight.hopper.api.Item
import dev.helight.hopper.external.ColorApi
import dev.helight.hopper.inventory.v1.Gui
import dev.helight.hopper.inventory.v1.InteractivePoint
import dev.helight.hopper.inventory.v1.annotation.NoGarbageCollection
import dev.helight.hopper.inventory.v1.routes.PagedBarRoute
import dev.helight.hopper.utilities.Chat
import dev.helight.hopper.utilities.Persistence
import org.apache.commons.lang.WordUtils
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.function.Consumer

@NoGarbageCollection
class ItemDataViewer(val viewed: ItemStack): Gui() {

    override fun construct() {
        if (viewed.itemMeta != null && viewed.itemMeta?.persistentDataContainer?.isEmpty == false) {
            val data = viewed.itemMeta!!.persistentDataContainer
            addNode("root", 4 + 18, InteractivePoint.switchView(
                Item.builder(Material.BOOKSHELF)
                    .name("§ePersistent Data")
                    .lore("§e${data.keys.size} §7Entries")
                    .delegate(),
                "persistent"
            ))

            val nbtRoute = PagedBarRoute(rows, this)
            nbtRoute.addBar(1, InteractivePoint.switchView(Item.builder(Material.ARROW).name("§fBack").delegate(), "root").apply {
                parent = this@ItemDataViewer
            })
            data.keys.forEachIndexed { index, value ->
                val hopperStringContent = Persistence.load(value.key, data)
                if (hopperStringContent != null) {

                    nbtRoute.put(index, InteractivePoint(
                        Item.builder(Material.HOPPER)
                            .name("§e${value.namespace}.${value.key}")
                            .lore(
                                "§7Type: §eHopper String",
                                "",
                                *WordUtils.wrap(hopperStringContent, 50, "\n", true).split("\n").map {
                                    "§8$it"
                                }.toTypedArray()
                            )
                            .delegate()
                    )
                    )
                    return@forEachIndexed
                }
                if (data.has(value, PersistentDataType.STRING)) {
                    nbtRoute.put(index, InteractivePoint(
                        Item.builder(Material.STRING)
                            .name("§e${value.namespace}.${value.key}")
                            .lore(
                                "§7Type: §eVanilla String",
                                "",
                                *WordUtils.wrap(data.get(value, PersistentDataType.STRING)!!, 50, "\n", true).split("\n").map {
                                    "§8$it"
                                }.toTypedArray()
                            )
                            .delegate()
                    )
                    )
                } else {
                    nbtRoute.put(index, InteractivePoint(
                        Item.builder(Material.COMPARATOR)
                            .name("§e${value.namespace}.${value.key}")
                            .lore(
                                "§7Type: §eUnknown"
                            )
                            .delegate()
                    )
                    )
                }
            }
            views["persistent"] = nbtRoute
        } else {
            addNode("root", 4 + 18, InteractivePoint(
                Item.builder(Material.BOOKSHELF)
                    .name("§ePersistent Data")
                    .lore("§7None")
                    .delegate()
            )
            )
        }


        val mat = viewed.type
        addNode("root", 3 + 9, InteractivePoint(
            item = Item.builder(mat).name("§e${mat.name}")
                .lore(
                    "§7Solid: " + boolToString(mat.isSolid),
                    "§7Interactable: " + boolToString(mat.isInteractable),
                    "§7Edible: " + boolToString(mat.isEdible),
                    "§7Occluding: " + boolToString(mat.isOccluding),
                    "§7Item: " + boolToString(mat.isItem),
                    "§7Block: " + boolToString(mat.isBlock),
                    "§7Flammable: " + boolToString(mat.isFlammable),
                    "§7Burnable: " + boolToString(mat.isBurnable),
                    "§7Fuel: " + boolToString(mat.isFuel),
                    "§7Blast Resistance: " + when(mat.isBlock) {
                        true -> "§a${mat.blastResistance}"
                        false -> "§8/"
                    },
                    "§7Hardness: " + when(mat.isBlock) {
                        true -> "§a${mat.hardness}"
                        false -> "§8/"
                    }
                )
                .delegate()
        )
        )

        val meta = viewed.itemMeta
        if (meta == null) {
            addNode("root", 5 + 9, InteractivePoint(
                item = Item.builder(Material.PAPER).name("§eMeta")
                    .lore(
                        "§7Not available"
                    )
                    .delegate()
            )
            )
        } else {
            addNode("root", 5 + 9, InteractivePoint(
                item = Item.builder(Material.PAPER).name("§eMeta")
                    .lore(
                        "§7Custom Model Data: " + when(meta.hasCustomModelData()) {
                            true -> "§e" + meta.customModelData
                            false -> "§8/"
                        },
                        "§7Unbreakable: §e" + meta.isUnbreakable,
                        "§7Enchantments: §7",
                        *(meta.enchants.map { "§7- " + it.key.key.namespace + "." + it.key.key.key + ": §e" + it.value }).toTypedArray(),
                        "§7Item Flags: §e" + meta.itemFlags.joinToString { it.name }
                    )
                    .delegate()
            ))

            if (meta is LeatherArmorMeta) {
                val lam = meta as LeatherArmorMeta
                addNode("root", 4 + 9, InteractivePoint.suspended(event = { loaded, event ->
                    if (loaded) {

                    }
                }) {
                    val color = ColorApi.get(lam.color)
                    this.event = Consumer {
                        val loc = it.whoClicked.location
                        loc.world!!.dropItem(loc, color.inkFlask())
                    }

                    Item.builder(mat)
                        .changeMetaFun {
                            return@changeMetaFun meta
                        }
                        .name("§eLeather Meta")
                        .lore(*color.generateLore())
                        .flag(ItemFlag.HIDE_DYE)
                        .delegate()
                })
            } else if (meta is PotionMeta) {
                val pm = meta as PotionMeta
                addNode("root", 4 + 9, InteractivePoint.suspended(event = { loaded, event ->
                    if (loaded) {

                    }
                }) {
                    Item.builder(mat)
                        .changeMetaFun {
                            return@changeMetaFun meta
                        }
                        .name("§ePotion Meta")
                        .lore(
                            *when (pm.hasColor()) {
                                true -> {
                                    val color = ColorApi.get(pm.color!!)
                                    this.event = Consumer {
                                        val loc = it.whoClicked.location
                                        loc.world!!.dropItem(loc, color.inkFlask())
                                    }
                                    arrayOf(Chat.line(24, center = " §8Color ")) + color.generateLore(false)
                                }
                                false -> arrayOf()
                            },
                            *when(pm.hasCustomEffects()) {
                                true -> arrayOf(Chat.line(24, center = " §8Effects ")) + pm.customEffects.map {
                                    it.type.name + " " + it.amplifier  + ": " + it.duration  + "(Ambient: ${boolToString(it.isAmbient)})"
                                }.toTypedArray()
                                false -> arrayOf()
                            }

                        )
                        .flag(ItemFlag.HIDE_DYE)
                        .delegate()
                })
            } else if (meta is SkullMeta) {

            }
        }


        createInventory()
    }

    fun boolToString(boolean: Boolean) = when(boolean) {
        true -> "§atrue"
        false -> "§cfalse"
    }

}