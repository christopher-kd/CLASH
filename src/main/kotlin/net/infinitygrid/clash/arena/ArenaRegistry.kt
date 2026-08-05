package net.infinitygrid.clash.arena

import net.infinitygrid.clash.CLASH
import java.io.File
import java.io.FileReader

class ArenaRegistry(private val plugin: CLASH) {

    private val arenas: MutableMap<String, ArenaData> = mutableMapOf()

    fun loadAll() {
        arenas.clear()
        val folder: File = plugin.fileManager.arenasFolder
        if (!folder.exists() || !folder.isDirectory) return
        folder.listFiles { f -> f.isFile && f.extension.equals("json", true) }
            ?.forEach { file ->
                runCatching {
                    FileReader(file).use { reader ->
                        val data = plugin.fileManager.gson.fromJson(reader, ArenaData::class.java)
                        arenas[file.nameWithoutExtension] = data
                        plugin.logger.info("Loaded arena: ${data.mapName}")
                    }
                }.onFailure { ex ->
                    plugin.logger.warning("Failed to load arena '${file.name}': ${ex.message}")
                }
            }
    }

    fun getAll(): Collection<ArenaData> = arenas.values

    fun get(name: String): ArenaData? = arenas[name]

    fun entries(): Map<String, ArenaData> = arenas.toMap()
}
