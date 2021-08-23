package dev.helight.hopper.ecs

import dev.helight.hopper.*
import dev.helight.hopper.ecs.data.ArchetypeSnapshot
import dev.helight.hopper.ecs.data.EntitySnapshot
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock

data class Archetype(
    val group: ComponentGroup
) {
    var ids: MutableList<EntityId> = mutableListOf()

    //TODO: Doesn't work fix the persistence somehow
    var data: MutableList<MutableList<ComponentData>> = mutableListOf()
    //val relations: MutableList<EntityId> = mutableListOf()

    private val rwLock = ReentrantReadWriteLock()

    private val r: ReentrantReadWriteLock.ReadLock = rwLock.readLock()

    private val w: ReentrantReadWriteLock.WriteLock = rwLock.writeLock()

    private fun componentIndex(id: ComponentID): Int = group.indexOf(id)

    val size
        get() = ids.size

    @ExperimentalUnsignedTypes
    fun snapshot(): ArchetypeSnapshot {
        r.lock()
        val entities = mutableListOf<EntitySnapshot>()
        val serializers = group.map { ecs.serializers[it] }.toList()
        try {
            ids.forEachIndexed { index, entityId ->
                val serializedData = data[index].mapIndexed { componentIndex, data ->
                    serializers[componentIndex]!!.serialize(data)
                }.toList()
                entities.add(EntitySnapshot(entityId.toULong(), serializedData))
            }
        } finally {
            r.unlock()
        }
        return ArchetypeSnapshot(group.joinToString("-"), entities)
    }

    fun push(entityId: EntityId, components: MutableList<ComponentData>) {
        w.lock()
        try {
            ids.add(entityId)
            data.add(components)
        } finally {
            w.unlock()
        }
    }

    fun pop(entityId: EntityId): ExportedEntity? {
        var currentData: ExportedEntity? = null
        w.lock()
        try {
            val index = ids.indexOf(entityId)
            if (index != -1) {
                currentData = Triple(entityId, group, data[index])
                ids.removeAt(index)
                data.removeAt(index)
            }
        } finally {
            w.unlock()
        }
        return currentData
    }

    fun update(entityId: EntityId, componentID: ComponentID, value: ComponentData) {
        w.lock()
        try {
            val index = ids.indexOf(entityId)
            val componentIndex = componentIndex(componentID)
            data[index][componentIndex] = value
        } finally {
            w.unlock()
        }
    }

    fun get(entityId: EntityId, componentID: ComponentID): ComponentData {
        val currentData: ComponentData
        r.lock()
        try {
            val index = ids.indexOf(entityId)
            val componentIndex = componentIndex(componentID)
            currentData = data[index][componentIndex]
        } finally {
            r.unlock()
        }
        return currentData
    }

    fun contains(entityId: EntityId): Boolean = ids.contains(entityId)

    fun export(entityId: EntityId): ExportedEntity? {
        r.lock()
        var currentData: ExportedEntity? = null
        try {
            val index = ids.indexOf(entityId)
            if (index != -1) {
                currentData = Triple(entityId, group, data[index])
            }
        } finally {
            r.unlock()
        }
        return currentData
    }

    fun all(): List<Pair<EntityId, MutableList<ComponentData>>> {
        val currentData: MutableList<Pair<EntityId, MutableList<ComponentData>>> = mutableListOf()
        r.lock()
        try {
            ids.forEachIndexed { index, id ->
                currentData.add(id to data[index])
            }
        } finally {
            r.unlock()
        }
        return currentData
    }

    fun print() {
        r.lock()
        ids.forEachIndexed {i,x ->
            println("[$i] ${ids[i]} = ${data[i]}")
        }
        r.unlock()
    }


    companion object {
        @ExperimentalUnsignedTypes
        fun loadFromSnapshot(snapshot: ArchetypeSnapshot): Archetype {
            val group = when (snapshot.group.isEmpty()) {
                false -> TreeSet(snapshot.group.split("-").map { it.toULong() })
                true -> sortedSetOf()
            }
            println(ecs.serializers.toString())
            val serializers = group.map { ecs.serializers[it] }.toList()
            val eList = mutableListOf<EntityId>()
            val dList = mutableListOf<MutableList<ComponentData>>()
            snapshot.entities.forEach {
                val deserializedData = it.data.mapIndexed { index, data ->
                    serializers[index]!!.deserialize(data)
                }.toMutableList()
                eList.add(it.id.toULong())
                dList.add(deserializedData)
            }
            val archetype = Archetype(group)
            archetype.ids = eList
            archetype.data = dList
            return archetype
        }
    }

}