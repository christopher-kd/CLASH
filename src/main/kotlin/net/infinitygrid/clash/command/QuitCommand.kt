package net.infinitygrid.clash.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.infinitygrid.clash.player.PlayerRegistry
import org.bukkit.entity.Player

class QuitCommand {

    fun createCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("quit").executes(::quit)
    }

    private fun quit(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as? Player ?: return Command.SINGLE_SUCCESS
        val clashPlayer = PlayerRegistry.instance.getPlayer(player) ?: return Command.SINGLE_SUCCESS

        val arena = clashPlayer.arena
        if (arena == null) {
            player.sendMessage("You are not in an arena.")
            return Command.SINGLE_SUCCESS
        }

        arena.leave(clashPlayer)
        player.sendMessage("You left the arena.")
        return Command.SINGLE_SUCCESS
    }
}
