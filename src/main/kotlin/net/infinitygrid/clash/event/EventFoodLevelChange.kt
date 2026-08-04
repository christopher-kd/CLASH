package net.infinitygrid.clash.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent

class EventFoodLevelChange : Listener {

    @EventHandler
    fun onFoodDeplete(event: FoodLevelChangeEvent) {
        event.isCancelled = true
    }

}