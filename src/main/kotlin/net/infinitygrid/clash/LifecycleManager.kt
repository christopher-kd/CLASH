package net.infinitygrid.clash

import net.infinitygrid.clash.arena.ArenaManager
import net.infinitygrid.clash.arena.ArenaRegistry
import net.infinitygrid.clash.util.CreatorNameCache
import net.infinitygrid.clash.config.FileManager
import net.infinitygrid.clash.event.*
import net.infinitygrid.clash.player.PlayerRegistry
import net.infinitygrid.clash.schematic.SchematicRegistry
import net.infinitygrid.clash.world.TemporaryWorldManager
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import java.util.UUID

class LifecycleManager(private val plugin: CLASH) {

    var temporaryWorldManager: TemporaryWorldManager? = null
        private set

    var fileManager: FileManager? = null
        private set

    var schematicRegistry: SchematicRegistry? = null
        private set

    var arenaRegistry: ArenaRegistry? = null
        private set

    var arenaManager: ArenaManager? = null
        private set

    var creatorNameCache: CreatorNameCache? = null
        private set

    fun enable() {
        fileManager = FileManager(plugin)
        schematicRegistry = SchematicRegistry(plugin).also { it.loadAll() }
        arenaRegistry = ArenaRegistry(plugin).also { it.loadAll() }
        temporaryWorldManager = TemporaryWorldManager(20)
        arenaManager = ArenaManager()

        val creatorUuids = arenaRegistry!!.getAll()
            .flatMap { it.mapCreators }
            .mapNotNull { runCatching { UUID.fromString(it) }.getOrNull() }
        creatorNameCache = CreatorNameCache().also { it.loadAll(creatorUuids) }

        val playerRegistry = PlayerRegistry.initialize()

        plugin.registerListener(
            PlayerSessionListener(),
            EventFoodLevelChange(),
            EventPlayerDamage(),
            AgilityListener(),
            SwitchWorldEvent(),
            InventoryEvents(),
            InventoryInteractEvent(),
            ArenaSelectionMenuListener()
        )

        Bukkit.getOnlinePlayers().forEach { bukkitPlayer ->
            playerRegistry.registerPlayer(bukkitPlayer)
            plugin.logger.info("Re-registered ${bukkitPlayer.name} after plugin reload.")
        }
        
        plugin.logger.info("CLASH initialized and ready!")
    }

    fun disable() {
        // Unregister all listeners for this plugin to avoid duplicate registrations on reload
        HandlerList.unregisterAll(plugin)
        
        PlayerRegistry.terminate()
        temporaryWorldManager?.clean()
        temporaryWorldManager = null
        schematicRegistry = null
        arenaRegistry = null
        arenaManager = null
        creatorNameCache = null
        fileManager = null
        plugin.logger.info("Plugin disabled.")
    }

    fun reload() {
        plugin.logger.info("Reloading CLASH...")
        disable()
        enable()
        plugin.logger.info("CLASH reloaded successfully.")
    }
}
