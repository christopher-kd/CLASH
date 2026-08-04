package net.infinitygrid.clash.player

import net.infinitygrid.clash.player.movement.MovementController
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerRegistry {

    companion object {
        private var _instance: PlayerRegistry? = null

        val instance: PlayerRegistry
            get() = _instance ?: throw IllegalStateException("PlayerRegistry instance not initialized")

        fun initialize(): PlayerRegistry {
            if (_instance == null) {
                _instance = PlayerRegistry()
            }
            return _instance!!
        }

        fun terminate() {
            _instance?.players?.forEach {
                it.groundTracker.stopGroundScheduler()
                it.movementController.stopCooldown()
            }
            _instance?.players?.clear()
            _instance = null
        }
    }

    private val players = ConcurrentHashMap.newKeySet<CLASHPlayer>()

    fun registerPlayer(bukkitPlayer: Player): CLASHPlayer {
        val player = CLASHPlayer(bukkitPlayer)
        players.add(player)
        return player
    }

    fun getPlayer(bukkitPlayer: Player) = players.find(bukkitPlayer::equals)

    fun getPlayerByUUID(uuid: UUID) = players.find { it.uniqueId == uuid }

    fun unregisterPlayer(bukkitPlayer: Player) {
        val player = getPlayer(bukkitPlayer)
        player?.groundTracker?.stopGroundScheduler()
        player?.movementController?.stopCooldown()
        players.removeIf(bukkitPlayer::equals)
    }

    fun unregisterAll() {
        players.forEach {
            it.groundTracker.stopGroundScheduler()
            it.movementController.stopCooldown()
        }
        players.clear()
    }

}