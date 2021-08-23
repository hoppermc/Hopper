package dev.helight.hopper.api

import dev.helight.hopper.HopperSpigotHook
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import java.util.*
import java.util.function.Supplier

abstract class BetterListener : Listener {

    private val uuid: UUID = UUID.randomUUID()
    fun register() {
        Bukkit.getPluginManager().registerEvents(this, HopperSpigotHook.plugin)
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
    }

    companion object {
        fun find(uuid: UUID): BetterListener? {
            for (registeredListener in HandlerList.getRegisteredListeners(HopperSpigotHook.plugin)) {
                val listener = registeredListener.listener
                if (listener is BetterListener) {
                    val helightListener = listener
                    if (helightListener.uuid === uuid) {
                        return helightListener
                    }
                }
            }
            return null
        }

        fun find(clazz: Class<out BetterListener?>): BetterListener? {
            for (registeredListener in HandlerList.getRegisteredListeners(HopperSpigotHook.plugin)) {
                val listener = registeredListener.listener
                if (clazz.isAssignableFrom(listener.javaClass)) {
                    return listener as BetterListener
                }
            }
            return null
        }

        fun <K : BetterListener?> assureRegistered(clazz: Class<K>, absenceSupplier: Supplier<K>) {
            val present = find(clazz)
            if (present == null) {
                absenceSupplier.get()!!.register()
            }
        }

        @Throws(IllegalAccessException::class, InstantiationException::class)
        fun <K : BetterListener?> assureRegistered(clazz: Class<K>) {
            val present: BetterListener? = find(clazz)
            if (present == null) {
                @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
                clazz.newInstance()!!.register()
            }
        }
    }
}