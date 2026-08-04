package net.infinitygrid.clash.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.player.PlayerRegistry
import net.infinitygrid.clash.player.setup.MapSetup
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.WorldCreator
import org.bukkit.entity.Player

class CLASHCommand {

    fun createCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        val root = Commands.literal("clash")
        root.then(Commands.literal("setup").then(Commands.literal("new").then(
            Commands.argument("Schematic to Load", StringArgumentType.string())
                .suggests { _, builder ->
                    CLASH.INSTANCE.schematicRegistry.getNames().forEach { name -> builder.suggest(name) }
                    builder.buildFuture()
                }
                .executes(::setup)
        )))

        root.then(
            Commands.literal("loadworld_test")
                .then(
                    Commands.argument("Name", StringArgumentType.word())
                        .executes(::loadWorld)
                )
        )

        root.then(
            Commands.literal("reload")
                .requires { it.sender.hasPermission("clash.reload") }
                .executes {
                    it.source.sender.sendMessage("Reloading CLASH...")
                    CLASH.INSTANCE.reload()
                    it.source.sender.sendMessage("CLASH reloaded successfully.")
                    Command.SINGLE_SUCCESS
                }
        )

        return root
    }

    private fun setup(ctx: CommandContext<CommandSourceStack>): Int {
        val executor = ctx.source.sender ?: return Command.SINGLE_SUCCESS
        val player = (executor as Player)
        player.sendMessage("running command! yay!!!")
        val schematicName = StringArgumentType.getString(ctx, "Schematic to Load")

        val setup = MapSetup(PlayerRegistry.instance.getPlayer(player)!!, schematicName)
        setup.prepareWorld()

        return Command.SINGLE_SUCCESS
    }

    private fun loadWorld(ctx: CommandContext<CommandSourceStack>): Int {
        val executor = ctx.source.sender ?: return Command.SINGLE_SUCCESS
        val worldName = StringArgumentType.getString(ctx, "Name")
        executor.sendMessage("Loading world: $worldName")
        val worldCreator = WorldCreator(worldName)
        val world = Bukkit.createWorld(worldCreator)
        (executor as Player).teleportAsync(Location(world, 0.0, 0.0, 0.0))
        return Command.SINGLE_SUCCESS
    }
}