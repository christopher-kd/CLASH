package net.infinitygrid.clash.player

import org.bukkit.entity.Player
import java.util.*
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
            _instance?.players?.values?.forEach {
                it.groundTracker.stopGroundScheduler()
                it.movementController.stopCooldown()
            }
            _instance?.players?.clear()
            _instance = null
        }
    }

    private val players = ConcurrentHashMap<UUID, CLASHPlayer>()

    fun registerPlayer(bukkitPlayer: Player): CLASHPlayer {
        val player = CLASHPlayer(bukkitPlayer)
        players[bukkitPlayer.uniqueId] = player
        return player
    }

    fun getPlayer(bukkitPlayer: Player) = players[bukkitPlayer.uniqueId]

    fun getPlayerByUUID(uuid: UUID) = players[uuid]

    fun unregisterPlayer(bukkitPlayer: Player) {
        val player = players.remove(bukkitPlayer.uniqueId)
        player?.groundTracker?.stopGroundScheduler()
        player?.movementController?.stopCooldown()
    }

    fun unregisterAll() {
        players.values.forEach {
            it.groundTracker.stopGroundScheduler()
            it.movementController.stopCooldown()
        }
        players.clear()
    }

}