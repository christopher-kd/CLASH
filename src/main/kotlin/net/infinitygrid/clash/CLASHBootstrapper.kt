package net.infinitygrid.clash

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.infinitygrid.clash.command.CLASHCommand
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage")
class CLASHBootstrapper : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            val command = CLASHCommand()
            commands.registrar().register(command.createCommand().build(), "eee")
        }
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return CLASH()
    }
}