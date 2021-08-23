package dev.helight.hopper.data

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.io.File
import java.io.IOException

class JsonFileConfiguration : ConfigurationSource {

    /**
     * Get the configuration of the source in async manner. The name of the config as well as the group
     * and eventual field documentation as well as field name and type graph should be derived via
     * reflections.
     */
    @Suppress("BlockingMethodInNonBlockingContext") //Delay is minimal, also blocking is outsourced to the IO Dispatcher
    @Throws(IOException::class)
    override fun <T> getConfiguration(clazz: Class<T>): T {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val directory = File("plugins", ConfigurationSourceHelper.configGroup(clazz))
        val file = File(directory, ConfigurationSourceHelper.configName(clazz) + ".json")
        if (!directory.exists()) directory.mkdir()
        if (!file.exists()) {
            file.createNewFile()
            val default = clazz.newInstance()
            val data = gson.toJson(default)
            val jsonObject = JsonParser().parse(data).asJsonObject
            val docsData = gson.toJson(ConfigurationSourceHelper.layout(clazz).map { it.key to it.comment }.toMap())
            val docsObject = JsonParser().parse(docsData).asJsonObject
            jsonObject.add("_docs", docsObject)
            file.writeText(gson.toJson(jsonObject))
        }
        return gson.fromJson(file.readText(), clazz)
    }

    override fun registeredId(): String = "JsonFile"

}