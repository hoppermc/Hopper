package dev.helight.hopper.inventory.v1

import dev.helight.hopper.api.BetterListener
import dev.helight.hopper.inventory.v1.impl.DefaultRouteImpl
import dev.helight.hopper.synchronizeDecoupled
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class Gui : InventoryHolder {

    var inv: Inventory? = null
    override fun getInventory(): Inventory  = inv!!

    val id: UUID
    val rows = 6

    abstract fun construct()

    var lastAction = System.nanoTime()

    val views: MutableMap<String, Route> = HashMap()

    val title = " "
    var current = "root"
    var offset = 0

    fun notifyAction() {
        lastAction = System.nanoTime()
    }

    fun show(entity: HumanEntity) {
        entity.openInventory(inv!!)
    }

    fun addNode(view: String, slot: Int, node: InteractivePoint): Gui {
        if (!views.containsKey(view)) {
            views[view] = DefaultRouteImpl()
        }
        node.parent = this
        views[view]!!.put(slot, node)
        return this
    }

    fun addNode(view: String, page: Int, slot: Int, node: InteractivePoint): Gui {
        if (!views.containsKey(view)) {
            views[view] = DefaultRouteImpl()
        }
        node.parent = this
        views[view]!!.put(slot + rows * 9 * page, node)
        return this
    }

    fun getNode(i: Int): InteractivePoint? {
        val index = i % (rows * 9)
        return views[current]!![i, index, currentPage()]
    }

    fun checkInitialisation() {
        BetterListener.assureRegistered(GuiEventListener::class.java)
    }

    fun dispose(reason: DisposeReason?) {
        //Close inventory for remaining viewers
        if (inv != null) {
            for (viewer in inv!!.viewers) {
                synchronizeDecoupled { viewer.closeInventory() }
            }
        }

        //Finalize
        inv = null
        cache.remove(id)
    }

    fun createInventory() {
        inv = Bukkit.createInventory(this, rows * 9, title)
        render()
    }

    fun pageSize(): Int {
        return rows * 9
    }

    fun changeOffset(difference: Int) {
        offset += difference * (rows * 9)
        render()
    }

    fun currentPage(): Int {
        return offset / (rows * 9)
    }

    fun changeView(view: String) {
        current = view
        offset = 0
        render()
    }

    fun findRelativeByActual(actual: Int): Int {
        return offset + actual
    }

    fun render() {
        val route = views[current]
        route!!.build()
        for (i in offset until offset + rows * 9) {
            val index = i % (rows * 9)
            val node = route[i, index, currentPage()]
            node?.build()
            inv!!.setItem(index, node?.item)
        }
    }

    enum class DisposeReason {
        NATURAL, PLUGIN, SECURITY, GARBAGE_COLLECTOR
    }

    companion object {
        //TODO Make value references weak
        val cache: MutableMap<UUID, Gui> = ConcurrentHashMap<UUID, Gui>()
    }

    init {
        id = UUID.randomUUID()
        cache[id] = this
    }
}