package net.infinitygrid.clash.player

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import java.util.UUID

class ScoreboardManager(val clashPlayer: CLASHPlayer) {

    val scoreboardManager = Bukkit.getScoreboardManager() ?: error("Failed to get scoreboard manager")
    val scoreboard = scoreboardManager.newScoreboard

    val objective = scoreboard.registerNewObjective(UUID.randomUUID().toString(), Criteria.DUMMY, text("CLASH"))

    init {
        objective.displaySlot = null
        clashPlayer.scoreboard = scoreboard
    }

    fun setTitle(title: Component) {
        objective.displayName(title)
    }

    fun setText(lines: List<String>) {
        var score = lines.size
        for (line in lines) {
            objective.getScore(line).score = score
            score--
        }
    }

    fun hide(hide: Boolean) {
        if (!hide) {
            objective.displaySlot = org.bukkit.scoreboard.DisplaySlot.SIDEBAR
        } else {
            objective.displaySlot = null
        }
    }


}