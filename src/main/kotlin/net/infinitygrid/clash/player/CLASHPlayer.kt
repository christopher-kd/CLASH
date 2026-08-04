package net.infinitygrid.clash.player

import net.infinitygrid.clash.player.movement.MovementController
import net.infinitygrid.clash.player.setup.MapSetup
import net.infinitygrid.clash.player.ui.HUDManager
import net.infinitygrid.clash.player.ui.interactivehotbar.InteractiveHotbar
import net.infinitygrid.clash.player.ui.interactivehotbar.InteractiveHotbarTemplates
import net.infinitygrid.clash.player.ux.PlayerSound
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
class CLASHPlayer(private val player: Player) : Player by player {

    val hudManager = HUDManager(this, 200)
    val groundTracker = GroundTracker(this)
    val movementController = MovementController(this)
    val soundPlayer = PlayerSound(this)
    val scoreboardManager = ScoreboardManager(this)
    val interactiveHotbar = InteractiveHotbar(this)
    var mapSetup: MapSetup? = null
        private set

    init {
        applyDefaultValues()
    }

    fun setupMap(mapSetup: MapSetup) {
        this.mapSetup = mapSetup
        player.gameMode = GameMode.CREATIVE
        InteractiveHotbarTemplates.applySetupMode(this)
    }

    fun cancelMapSetup() {
        this.mapSetup = null
    }

    private fun applyDefaultValues() {
        player.gameMode = GameMode.SURVIVAL
        player.allowFlight = true
        hudManager.updateCooldown(hudManager.maxCooldownTicks)
        player.foodLevel = 20
        player.getAttribute(Attribute.ATTACK_SPEED)?.baseValue = Double.MAX_VALUE
        applyHealthScale(3.0)
    }

    fun applyHealthScale(hearts: Double) {
        player.isHealthScaled = true
        player.healthScale = hearts * 2
    }

    override fun isOnGround(): Boolean {
        return groundTracker.isOnGround()
    }

}