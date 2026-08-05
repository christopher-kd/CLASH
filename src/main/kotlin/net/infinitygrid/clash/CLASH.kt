package net.infinitygrid.clash

import net.infinitygrid.clash.arena.ArenaManager
import net.infinitygrid.clash.arena.ArenaRegistry
import net.infinitygrid.clash.arena.CreatorNameCache
import net.infinitygrid.clash.config.FileManager
import net.infinitygrid.clash.schematic.SchematicRegistry
import net.infinitygrid.clash.world.TemporaryWorldManager
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class CLASH : JavaPlugin() {

    companion object {
        lateinit var INSTANCE: CLASH
            private set
    }

    private lateinit var lifecycleManager: LifecycleManager

    val temporaryWorldManager: TemporaryWorldManager
        get() = lifecycleManager.temporaryWorldManager
            ?: error("TemporaryWorldManager is not available (plugin disabled or still enabling).")

    val fileManager: FileManager
        get() = lifecycleManager.fileManager
            ?: error("FileManager is not available (plugin disabled or still enabling).")

    val schematicRegistry: SchematicRegistry
        get() = lifecycleManager.schematicRegistry
            ?: error("SchematicRegistry is not available (plugin disabled or still enabling).")

    val arenaRegistry: ArenaRegistry
        get() = lifecycleManager.arenaRegistry
            ?: error("ArenaRegistry is not available (plugin disabled or still enabling).")

    val arenaManager: ArenaManager
        get() = lifecycleManager.arenaManager
            ?: error("ArenaManager is not available (plugin disabled or still enabling).")

    val creatorNameCache: CreatorNameCache
        get() = lifecycleManager.creatorNameCache
            ?: error("CreatorNameCache is not available (plugin disabled or still enabling).")

    fun registerListener(vararg listeners: Listener) {
        listeners.forEach {
            logger.info("Registering listener: ${it.javaClass.simpleName}")
            Bukkit.getPluginManager().registerEvents(it, this)
        }
    }

    override fun onEnable() {
        INSTANCE = this
        lifecycleManager = LifecycleManager(this)
        lifecycleManager.enable()
    }

    override fun onDisable() {
        lifecycleManager.disable()
    }

    fun reload() {
        lifecycleManager.reload()
    }

}
