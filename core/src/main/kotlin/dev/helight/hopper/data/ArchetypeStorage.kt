package dev.helight.hopper.data

import dev.helight.hopper.*
import dev.helight.hopper.ecs.data.StoreSnapshot
import dev.helight.hopper.extensions.ComponentGroupExtensions.migrateMultipleDown
import dev.helight.hopper.extensions.ComponentGroupExtensions.migrateTo
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock


@ExperimentalUnsignedTypes
class ArchetypeStorage {

    var mappedArchetypes: MutableMap<ComponentGroup, Archetype> = ConcurrentHashMap()
    var globalEntityList: MutableList<EntityId> = mutableListOf()

    private val rwLock = ReentrantReadWriteLock()

    private val r: ReentrantReadWriteLock.ReadLock = rwLock.readLock()

    private val w: ReentrantReadWriteLock.WriteLock = rwLock.writeLock()

    fun backup(): StoreSnapshot {
        r.lock()
        val snapshot: StoreSnapshot?
        try {
            snapshot = StoreSnapshot(mappedArchetypes.values.filterNot {
                it.group.contains(TransientEntity::class.java.toKey())
            }.map {
                val snap = it.snapshot()
                println("| $snap")
                snap
            }.toList())
        } finally {
            r.unlock()
        }
        return snapshot!!
    }

    fun load(storeSnapshot: StoreSnapshot) {
        storeSnapshot.archetypes.map(Archetype.Companion::loadFromSnapshot).forEach { archetype ->
            archetype.all().forEach {
                addEntity(it.first, archetype.group, it.second.toMutableList())
            }
        }
    }

    fun clear() {
        w.lock()
        try {
            mappedArchetypes.clear()
            globalEntityList.clear()
        } finally {
            w.unlock()
        }
    }

    fun getArchetype(group: ComponentGroup): Archetype = when (val archetype: Archetype? = mappedArchetypes[group]) {
        null -> {
            val created = Archetype(group)
            mappedArchetypes[group] = created
            created
        }
        else -> archetype
    }

    fun addEntity(id: EntityId, group: ComponentGroup = sortedSetOf(), data: MutableList<ComponentData> = mutableListOf()) {
        w.lock()
        globalEntityList.add(id)
        w.unlock()
        if (data.size != group.size) {
            for (i: Int in 0 until group.size) {
                data.add(null)
            }
        }
        getArchetype(group).push(id, data)
    }

    fun removeEntity(entityId: EntityId): ExportedEntity? {
        w.lock()
        globalEntityList.remove(entityId)
        w.unlock()
        for (archetype in mappedArchetypes.values) {
            val export = archetype.pop(entityId)
            if (export != null) {
                return export
            }
        }
        return null
    }

    fun peekAll(componentGroup: ComponentGroup): List<ExportedEntity> = queryEntityIds(*componentGroup.toULongArray()).mapNotNull {
        getEntity(it.first)
    }

    fun removeAll(componentGroup: ComponentGroup): List<ExportedEntity> = queryEntityIds(*componentGroup.toULongArray()).mapNotNull {
        removeEntity(it.first)
    }

    fun addComponent(id: EntityId, componentID: ComponentID, value: ComponentData) {
        val (_, group, data) = removeEntity(id)!!
        val newGroup = TreeSet(group)
        newGroup.add(componentID)
        val newData = group.migrateTo(newGroup, data, listOf(
            componentID to value
        )).toMutableList()
        addEntity(id,newGroup, newData)
    }

    fun updateComponent(id: EntityId, componentID: ComponentID, value: ComponentData) {
        val (_, group, _) = getEntity(id)!!
        mappedArchetypes[group]!!.update(id, componentID, value)
    }

    fun removeComponent(id: EntityId, componentID: ComponentID) {
        val (_, group, data) = removeEntity(id)!!
        val newGroup = TreeSet(group)
        newGroup.remove(componentID)
        val newData = group.migrateTo(newGroup, data, listOf()).toMutableList()
        addEntity(id,newGroup, newData)
    }

    fun getEntity(entityId: EntityId): ExportedEntity? {
        for (archetype in mappedArchetypes.values) {
            val export = archetype.export(entityId)
            if (export != null) {
                return export
            }
        }
        return null
    }

    fun queryArchetypes(vararg components: ComponentID): List<Archetype> = mappedArchetypes.filter {
        it.key.intersect(components).size == components.size
    }.map { it.value }.toList()

    fun queryEntityIds(vararg components: ComponentID): List<AssociatedEntity> = queryArchetypes(*components).flatMap { archetype ->
        archetype.ids.map {
            (it to archetype)
        }
    }

    fun queryEntities(group: ComponentGroup) = queryArchetypes(*group.toULongArray()).flatMap { archetype ->
        archetype.group.migrateMultipleDown(group, archetype.all())
    }

    fun queryEntitiesExpanded(group: ComponentGroup) = queryArchetypes(*group.toULongArray()).flatMap(Archetype::all)

    fun containsEntity(id: EntityId): Boolean {
        r.lock()
        val value = globalEntityList.contains(id)
        r.unlock()
        return value
    }

    companion object {
        fun restore(storeSnapshot: StoreSnapshot): ArchetypeStorage {
            val storage = ArchetypeStorage()
            storeSnapshot.archetypes.map(Archetype.Companion::loadFromSnapshot).forEach { archetype ->
                archetype.all().forEach {
                    storage.addEntity(it.first, archetype.group, it.second.toMutableList())
                }
            }
            return storage
        }
    }

}