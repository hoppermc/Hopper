package dev.helight.hopper.registry

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Multimaps
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

class Registry<R : Registrable> {
    private val instanceList = Collections.synchronizedList(ArrayList<R>())
    private val idIndexedMap: ListMultimap<String, R> = Multimaps.synchronizedListMultimap(ArrayListMultimap.create())
    private val uuidIndexedMap: MutableMap<UUID, R> = ConcurrentHashMap()

    fun register(r: R) {
        instanceList.add(r)
        idIndexedMap.put(r.registeredId(), r)
        uuidIndexedMap[r.registeredUuid()] = r
    }

    fun unregister(r: R) {
        instanceList.remove(r)
        idIndexedMap.remove(r.registeredId(), r)
        uuidIndexedMap[r.registeredUuid()] = r
    }

    fun containsValue(r: R): Boolean {
        return instanceList.contains(r)
    }

    fun findByUuid(uuid: UUID?): R? {
        return uuidIndexedMap[uuid]
    }

    fun findById(id: String?): List<R> {
        return idIndexedMap[id]
    }

    fun all(): List<R> {
        return instanceList
    }

    fun stream(): Stream<R> {
        return instanceList.stream()
    }

    fun truncate() {
        instanceList.clear()
        uuidIndexedMap.clear()
        idIndexedMap.clear()
    }
}


class SimpleRegistry<R : SimpleRegistrable> {
    private val instanceList = Collections.synchronizedList(ArrayList<R>())

    fun register(r: R) {
        instanceList.add(r)
    }

    fun unregister(r: R) {
        instanceList.remove(r)
    }

    fun containsValue(r: R): Boolean {
        return instanceList.any { it.registeredId() == r.registeredId() }
    }

    fun findById(id: String): R? {
        return instanceList.firstOrNull { it.registeredId() == id }
    }

    fun all(): List<R> {
        return instanceList
    }

    fun stream(): Stream<R> {
        return instanceList.stream()
    }

    fun truncate() {
        instanceList.clear()
    }
}