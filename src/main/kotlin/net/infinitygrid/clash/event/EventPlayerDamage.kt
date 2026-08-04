package net.infinitygrid.clash.event

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class EventPlayerDamage : Listener {

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        if (event.entityType != EntityType.PLAYER) return
        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            event.isCancelled = true
        }
        if (event.cause == EntityDamageEvent.DamageCause.VOID) {
            (event.entity as org.bukkit.entity.Player).health = 0.0
        }
    }

}