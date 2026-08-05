package net.infinitygrid.clash.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.infinitygrid.clash.CLASH
import java.io.File
import java.io.FileWriter

class FileManager(private val plugin: CLASH) {

    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    val schematicsFolder = File(plugin.dataFolder, "schematics")
    val worldTemplatesFolder = File(plugin.dataFolder, "world_templates")
    val arenasFolder = File(plugin.dataFolder, "arenas")
    private val configFile = File(plugin.dataFolder, "config.json")

    init {
        createFolders()
        createDefaultConfig()
    }

    private fun createFolders() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs()
        }
        
        if (!worldTemplatesFolder.exists()) {
            worldTemplatesFolder.mkdirs()
        }

        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs()
        }
    }

    private fun createDefaultConfig() {
        if (!configFile.exists()) {
            try {
                val emptyConfig = mutableMapOf<String, Any>()
                FileWriter(configFile).use { writer ->
                    gson.toJson(emptyConfig, writer)
                }
                plugin.logger.info("Created default config.json")
            } catch (e: Exception) {
                plugin.logger.severe("Could not create config.json: ${e.message}")
            }
        }
    }
}
