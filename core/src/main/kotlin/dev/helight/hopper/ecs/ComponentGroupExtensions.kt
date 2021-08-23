package dev.helight.hopper.ecs

import dev.helight.hopper.ComponentData
import dev.helight.hopper.ComponentGroup
import dev.helight.hopper.ComponentID
import dev.helight.hopper.EntityId

object ComponentGroupExtensions {
    fun ComponentGroup.migrateTo(other: ComponentGroup, data: List<ComponentData>, addition: List<Pair<ComponentID, ComponentData>>): List<ComponentData> {
        val intersection = other.intersect(this)
        val bArr = arrayOfNulls<ComponentData>(other.size)
        intersection.forEach {
            val aIndex = this.indexOf(it)
            val bIndex = other.indexOf(it)
            bArr[bIndex] = data[aIndex]
        }
        addition.forEach {
            val bIndex = other.indexOf(it.first)
            bArr[bIndex] = it.second
        }
        return bArr.toMutableList()
    }

    fun ComponentGroup.migrateMultipleDown(other: ComponentGroup, data: List<Pair<EntityId, List<ComponentData>>>): List<Pair<EntityId, MutableList<ComponentData>>> {
        val intersection = other.intersect(this)
        val mapper = intersection.map {
            val aIndex = this.indexOf(it)
            val bIndex = other.indexOf(it)
            aIndex to bIndex
        }.sortedBy { it.second }
        return data.map { superPair ->
            val id = superPair.first
            val srcData = superPair.second
            val out = mutableListOf<ComponentData>()
            mapper.forEach {
                out.add(srcData[it.first])
            }
            id to out
        }.toList()
    }
}