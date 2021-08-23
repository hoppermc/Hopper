package dev.helight.hopper

import dev.helight.hopper.ecs.Archetype
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ArchetypeTest {

    @Test
    fun test1() {
        val g1: ComponentGroup = sortedSetOf(0UL, 1UL, 2UL)
        val g2: ComponentGroup = sortedSetOf(0UL, 2UL)
        val archetype = Archetype(g1)
        val e1 = 100UL
        val e2 = 200UL
        val secondArchetype = Archetype(g2)
        archetype.push(e1, mutableListOf("1", "2", "3"))
        archetype.push(e2, mutableListOf("A", "B", "C"))
        assertEquals(archetype.get(e1, 1UL), "2")
        assertEquals(archetype.get(e2, 1UL), "B")
        archetype.print() //DEBUG
        val exported = archetype.pop(e1)
        archetype.print() //DEBUG
        assertEquals(archetype.size, 1)
        secondArchetype.print() //DEBUG
        //secondArchetype.push(e1, g1.migrateTo(g2, exported!!, mutableListOf()))
        assertEquals(secondArchetype.get(e1, 0UL), "1")
        assertEquals(secondArchetype.get(e1, 2UL), "3")
        secondArchetype.print() //DEBUG
    }

}