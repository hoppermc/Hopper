package dev.helight.hopper.registry

import java.util.*

interface Registrable {
    fun registeredId(): String
    fun registeredUuid(): UUID
}

interface SimpleRegistrable {
    fun registeredId(): String
}