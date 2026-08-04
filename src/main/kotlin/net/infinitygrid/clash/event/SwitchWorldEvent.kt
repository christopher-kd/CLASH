package net.infinitygrid.clash.event

import net.infinitygrid.clash.CLASH
import net.infinitygrid.clash.world.TemporaryWorldState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

class SwitchWorldEvent : Listener {

    @EventHandler
    fun onSwitchWorld(event: PlayerChangedWorldEvent) {
        CLASH.INSTANCE.temporaryWorldManager?.createdWorlds?.find { it.uid == event.player.world.uid }?.state = TemporaryWorldState.OCCUPIED

    }

}