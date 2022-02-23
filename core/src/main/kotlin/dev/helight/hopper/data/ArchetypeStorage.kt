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
        try {
            globalEntityList.add(id)
            if (data.size != group.size) {
                for (i: Int in 0 until group.size) {
                    data.add(null)
                }
            }
            getArchetype(group).push(id, data)
        } finally {
            w.unlock()
        }
    }

    fun removeEntity(entityId: EntityId): ExportedEntity? {
        w.lock()
        globalEntityList.remove(entityId)
        try {
            for (archetype in mappedArchetypes.values) {
                val export = archetype.pop(entityId)
                if (export != null) {
                    return export
                }
            }
        } finally {
            w.unlock()
        }
        return null
    }

    fun replaceEntity(exportedEntity: ExportedEntity) {
        w.lock()
        try {
            var hasDeleted = false
            for (archetype in mappedArchetypes.values) {
                val export = archetype.pop(exportedEntity.first)
                if (export != null) {
                    hasDeleted = true
                }
            }
            if (!hasDeleted) error("Can't replace entity ${exportedEntity.first} because it doesn't currently exist")
            val newArchetype = getArchetype(exportedEntity.second)
            newArchetype.push(exportedEntity.first, exportedEntity.third)
        } finally {
            w.unlock()
        }
    }

    fun peekAll(componentGroup: ComponentGroup): List<ExportedEntity> {
        r.lock()
        return try {
            queryEntityIds(*componentGroup.toULongArray()).mapNotNull {
                getEntity(it.first)
            }
        } finally {
            r.unlock()
        }
    }

    fun removeAll(componentGroup: ComponentGroup): List<ExportedEntity> = queryEntityIds(*componentGroup.toULongArray()).mapNotNull {
        removeEntity(it.first)
    }

    fun addComponent(id: EntityId, componentID: ComponentID, value: ComponentData) {
        w.lock()
        try {
            val (_, group, data) = removeEntity(id)!!
            val newGroup = TreeSet(group)
            newGroup.add(componentID)
            val newData = group.migrateTo(newGroup, data, listOf(
                componentID to value
            )).toMutableList()
            addEntity(id,newGroup, newData)
        } finally {
            w.unlock()
        }
    }

    fun updateComponent(id: EntityId, componentID: ComponentID, value: ComponentData) {
        r.lock()
        try {
            var updated = false
            mappedArchetypes.forEach {
                if (it.value.update(id, componentID, value)) updated = true
            }
            if (!updated) error("No archetype contained an entity with the id $id")
        } finally {
            r.unlock()
        }
    }

    fun transformComponent(id: EntityId, componentID: ComponentID, transformer: ComponentTransformer) {
        r.lock()
        try {
            var updated = false
            mappedArchetypes.forEach {
                if (it.value.transform(id, componentID, transformer)) updated = true
            }
            if (!updated) error("No archetype contained an entity with the id $id")
        } finally {
            r.unlock()
        }
    }

    fun removeComponent(id: EntityId, componentID: ComponentID) {
        w.lock()
        try {
            val (_, group, data) = removeEntity(id)!!
            val newGroup = TreeSet(group)
            newGroup.remove(componentID)
            val newData = group.migrateTo(newGroup, data, listOf()).toMutableList()
            addEntity(id,newGroup, newData)
        } finally {
            w.unlock()
        }
    }

    fun getEntity(entityId: EntityId): ExportedEntity? {
        r.lock()
        try {
            for (archetype in mappedArchetypes.values) {
                val export = archetype.export(entityId)
                if (export != null) {
                    return export
                }
            }
        } finally {
            r.unlock()
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

    fun queryEntities(group: ComponentGroup): List<Pair<EntityId, MutableList<ComponentData>>> {
        r.lock()
        return try {
            queryArchetypes(*group.toULongArray()).flatMap { archetype ->
                archetype.group.migrateMultipleDown(group, archetype.all())
            }
        } finally {
            r.unlock()
        }
    }

    fun queryEntitiesExpanded(group: ComponentGroup): List<ExportedEntity> {
        r.lock()
        return try {
            queryArchetypes(*group.toULongArray()).flatMap { archetype ->
                archetype.all().map {
                    ExportedEntity(it.first, archetype.group, it.second)
                }
            }
        } finally {
            r.unlock()
        }
    }

    fun containsEntity(id: EntityId): Boolean {
        r.lock()
        return try {
            globalEntityList.contains(id)
        } finally {
            r.unlock()
        }
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