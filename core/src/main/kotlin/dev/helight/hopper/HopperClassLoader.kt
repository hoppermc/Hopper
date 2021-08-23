package dev.helight.hopper

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URLClassLoader

@OptIn(ExperimentalUnsignedTypes::class)
class HopperClassLoader : ClassLoader(HopperEngine::class.java.classLoader) {

    val plugins: MutableList<HopperPlugin> = mutableListOf()

    fun loadAll() {
        val directory = File("hopperPlugins")
        if (!directory.exists()) directory.mkdir()
        directory.listFiles()!!.forEach(this::loadJar)
    }

    fun loadJar(jar: File) {
        val loader = URLClassLoader(arrayOf(jar.toURI().toURL()), this)
        val stream = loader.getResource("hopper.json")!!.openStream()
        val reader = stream.reader()
        val configuration = Json.decodeFromString<HopperPluginConfiguration>(reader.readText())
        reader.close()
        stream.close()
        println(configuration)
        val plugin = (loader.loadClass(configuration.mainClass).newInstance()) as HopperPlugin
        plugin.load()
        plugins.add(plugin)
    }

    fun enableAll() {
        plugins.forEach { it.enable() }
    }

    fun disableAll() {
        plugins.forEach { it.disable() }
    }

}