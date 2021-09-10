package dev.helight.hopper

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.reflections.Reflections
import org.reflections.scanners.FieldAnnotationsScanner
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import java.io.File
import java.net.URLClassLoader

@OptIn(ExperimentalUnsignedTypes::class)
class HopperSuperClassLoader : ClassLoader(HopperEngine::class.java.classLoader) {

    var plugins: MutableList<PluginContext> = mutableListOf()

    fun loadAll() {
        val directory = File("hopperPlugins")
        if (!directory.exists()) directory.mkdir()
        plugins = directory.listFiles { _, name -> name.endsWith(".jar") }!!.map(this::loadJar).sortedByDescending {
            it.configuration.loadPriority
        }.toMutableList()
        doLoadAll()
    }

    fun loadJar(jar: File): PluginContext {
        val loader = URLClassLoader(arrayOf(jar.toURI().toURL()), this)
        val stream = loader.getResource("hopper.json")!!.openStream()
        val reader = stream.reader()
        val configuration = Json.decodeFromString<HopperPluginConfiguration>(reader.readText())
        reader.close()
        stream.close()
        println(configuration)
        val plugin = (loader.loadClass(configuration.mainClass).newInstance()) as HopperPlugin
        val reflections = Reflections(configuration.namespace, loader,
            SubTypesScanner(false),
            TypeAnnotationsScanner(),
            FieldAnnotationsScanner(),
            MethodAnnotationsScanner(),
        )
        val context = PluginContext(plugin, configuration, reflections)
        reflections.allTypes.forEach {
            println("> $it")
        }
        return context
    }

    private fun doLoadAll() {
        plugins.forEach {
            it.reflections.getTypesAnnotatedWith(AnnotatedPluginRegistrant::class.java).forEach {
                println("Constructing plugin registrant ${it.name}")
                it.newInstance()
            }
            it.plugin.load()
        }
    }

    fun enableAll() {
        plugins.forEach { pl ->
            pl.reflections.getTypesAnnotatedWith(CommandAlias::class.java).forEach {
                println("Registering command '${it.name}'")
                hopper.spigot.commandManager.registerCommand(it.newInstance() as BaseCommand)
            }
            pl.plugin.enable()
        }
    }

    fun disableAll() {
        plugins.forEach { it.plugin.disable() }
    }
}

data class PluginContext(
    val plugin: HopperPlugin,
    val configuration: HopperPluginConfiguration,
    val reflections: Reflections,
)