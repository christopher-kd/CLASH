package net.infinitygrid.clash.schematic

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import net.infinitygrid.clash.CLASH
import java.io.File
import java.io.FileInputStream

class SchematicRegistry(private val plugin: CLASH) {

    private val schematics: MutableMap<String, Clipboard> = mutableMapOf()

    fun loadAll() {
        schematics.clear()
        val folder: File = plugin.fileManager.schematicsFolder
        if (!folder.exists() || !folder.isDirectory) return
        folder.listFiles { f -> f.isFile && (f.extension.equals("schem", true) || f.extension.equals("schematic", true)) }
            ?.forEach { file ->
                runCatching {
                    val format = ClipboardFormats.findByFile(file) ?: return@forEach
                    FileInputStream(file).use { fis ->
                        val clipboard = format.getReader(fis).read()
                        val name = file.nameWithoutExtension
                        schematics[name] = clipboard
                        plugin.logger.info("Loaded schematic: $name")
                    }
                }.onFailure { ex ->
                    plugin.logger.warning("Failed to load schematic '${file.name}': ${ex.message}")
                }
            }
    }

    fun getClipboard(name: String): Clipboard? = schematics[name]

    fun getNames(): Set<String> = schematics.keys
}
