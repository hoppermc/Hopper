package dev.helight.hopper.data

import dev.helight.hopper.registry.SimpleRegistrable
import dev.helight.hopper.registry.SimpleRegistry

interface ConfigurationSource : SimpleRegistrable {

    fun <T> getConfiguration(clazz: Class<T>): T

    companion object {

        @Suppress("MemberVisibilityCanBePrivate")
        val registry = SimpleRegistry<ConfigurationSource>().apply {
            register(JsonFileConfiguration())
        }

        val defaultSource: ConfigurationSource
            get() = registry.findById(defaultSourceType)!!

        @Suppress("MemberVisibilityCanBePrivate")
        var defaultSourceType = "JsonFile"

    }
}