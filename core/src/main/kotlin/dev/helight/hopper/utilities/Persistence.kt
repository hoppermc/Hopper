package dev.helight.hopper.utilities

import com.google.gson.Gson
import dev.helight.hopper.HopperSpigotHook
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.*

object Persistence {

    fun <K : PersistentDataContainer?> store(key: String, value: String, k: K): K {
        k!!.set(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING, value)
        return k
    }

    fun <V : PersistentDataContainer?> load(key: String, v: V): String? {
        return v!!.get(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING)
    }

    fun <V : PersistentDataContainer?> has(key: String, v: V): Boolean {
        return v!!.has(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING)
    }

    fun <V : PersistentDataContainer?> has(clazz: Class<*>, v: V): Boolean {
        return v!!.has(NamespacedKey(HopperSpigotHook.plugin, clazz.name.lowercase(Locale.getDefault())), PersistentDataType.STRING)
    }

    fun <K : PersistentDataContainer?> storeJson(o: Any, k: K): K {
        val gson = Gson()
        k!!.set(NamespacedKey(HopperSpigotHook.plugin, o.javaClass.name.lowercase(Locale.getDefault())),
            PersistentDataType.STRING,
            gson.toJson(o))
        return k
    }

    fun <K : PersistentDataContainer?> storeJson(key: String, o: Any?, k: K): K {
        val gson = Gson()
        k!!.set(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING, gson.toJson(o))
        return k
    }

    fun <K, V : PersistentDataContainer?> loadJson(clazz: Class<K>, v: V): K? {
        val gson = Gson()
        return try {
            gson.fromJson(v!!.get(NamespacedKey(HopperSpigotHook.plugin, clazz.name.lowercase(Locale.getDefault())),
                PersistentDataType.STRING), clazz)
        } catch (ignored: NullPointerException) {
            null
        }
    }

    fun <K, V : PersistentDataContainer?> loadJson(key: String, clazz: Class<K>?, v: V): K? {
        val gson = Gson()
        return try {
            gson.fromJson(v!!.get(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING),
                clazz)
        } catch (ignored: NullPointerException) {
            ignored.printStackTrace()
            null
        }
    }
    ///

    fun <K : PersistentDataContainer> K.store(key: String, value: String) {
        set(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING, value)
    }

    fun <V : PersistentDataContainer> V.load(key: String): String? {
        return get(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING)
    }

    fun <V : PersistentDataContainer> V.has(key: String): Boolean {
        return has(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING)
    }

    fun <V : PersistentDataContainer> V.has(clazz: Class<*>): Boolean {
        return has(NamespacedKey(HopperSpigotHook.plugin, clazz.name.lowercase(Locale.getDefault())), PersistentDataType.STRING)
    }

    fun <K : PersistentDataContainer> K.storeJson(o: Any) {
        val gson = Gson()
        set(NamespacedKey(HopperSpigotHook.plugin, o.javaClass.name.lowercase(Locale.getDefault())),
            PersistentDataType.STRING,
            gson.toJson(o))
    }

    fun <K : PersistentDataContainer> K.storeJson(key: String, o: Any?) {
        val gson = Gson()
        set(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING, gson.toJson(o))
    }

    fun <K, V : PersistentDataContainer> V.loadJson(clazz: Class<K>): K? {
        val gson = Gson()
        return try {
            gson.fromJson(get(NamespacedKey(HopperSpigotHook.plugin, clazz.name.lowercase(Locale.getDefault())),
                PersistentDataType.STRING), clazz)
        } catch (ignored: NullPointerException) {
            null
        }
    }

    fun <K, V : PersistentDataContainer> V.loadJson(key: String, clazz: Class<K>?): K? {
        val gson = Gson()
        return try {
            gson.fromJson(get(NamespacedKey(HopperSpigotHook.plugin, key.lowercase(Locale.getDefault())), PersistentDataType.STRING),
                clazz)
        } catch (ignored: NullPointerException) {
            ignored.printStackTrace()
            null
        }
    }

}