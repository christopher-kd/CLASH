package net.infinitygrid.clash.world

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.function.pattern.Pattern
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.block.BlockTypes
import net.infinitygrid.clash.CLASH
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import java.awt.Color
import org.bukkit.World
import java.time.Duration
import java.util.concurrent.CompletableFuture

enum class TemporaryWorldState() {
    FREE, OCCUPIED
}

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
class TemporaryWorld(val bukkitWorld: World) : World by bukkitWorld {

    var state = TemporaryWorldState.FREE
        set(value) {
            field = value
            if (value == TemporaryWorldState.FREE) {
                val mainSpawn = Bukkit.getWorlds()[0].spawnLocation
                bukkitWorld.players.forEach { it.teleportAsync(mainSpawn) }
            }
        }
    private var pastedRegion: CuboidRegion? = null

    fun fromSchematicAsync(schematicName: String): CompletableFuture<Unit> {
        val startingTime = System.currentTimeMillis()

        val frames = listOf(
            text("/"),
            text("-"),
            text("\\"),
            text("-")
        )
        val frameIndex = java.util.concurrent.atomic.AtomicInteger(0)

        val task = Bukkit.getScheduler().runTaskTimer(CLASH.INSTANCE, Runnable {
            val currentFrame = frameIndex.getAndIncrement()
            val baseTitle = "Entering Setup Mode"
            val titleBuilder = text()

            baseTitle.forEachIndexed { i, char ->
                val hue = (currentFrame * 0.05f + (baseTitle.length - 1 - i).toFloat() / baseTitle.length) % 1f
                val color = TextColor.color(Color.HSBtoRGB(hue, 1f, 1f) and 0xFFFFFF)
                titleBuilder.append(text(char.toString(), color))
            }

            val title = Title.title(
                titleBuilder.build(),
                frames[currentFrame % frames.size],
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1000), Duration.ofMillis(200))
            )
            Bukkit.getServer().showTitle(title)
        }, 1, 1)

        return CompletableFuture.supplyAsync {
            try {
                val clipboard = CLASH.INSTANCE.schematicRegistry.getClipboard(schematicName)!!
                val pasteAt = BlockVector3.at(0, 150, 0)

                val editSession = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(BukkitAdapter.adapt(this.bukkitWorld)).build()

                val operation = ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(pasteAt)
                    .ignoreAirBlocks(true)
                    .build()

                Operations.complete(operation)
                editSession.close()

                val offset = pasteAt.subtract(clipboard.origin)
                pastedRegion = CuboidRegion(
                    clipboard.region.minimumPoint.add(offset),
                    clipboard.region.maximumPoint.add(offset)
                )

                task.cancel()
                val finalTitle = Title.title(
                    text("Setup Mode").color(TextColor.fromHexString("#00FF00")),
                    text(schematicName),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))
                )
                Bukkit.getServer().showTitle(finalTitle)
            } catch (e: Exception) {
                task.cancel()
                println(e)
            }
            Unit
        }
    }

    fun resetAndFreeAsync(): CompletableFuture<Unit> {
        val region = pastedRegion
        pastedRegion = null

        if (region == null) {
            state = TemporaryWorldState.FREE
            return CompletableFuture.completedFuture(Unit)
        }

        return CompletableFuture.supplyAsync {
            val editSession = WorldEdit.getInstance().newEditSessionBuilder()
                .world(BukkitAdapter.adapt(this.bukkitWorld)).build()
            editSession.setBlocks(region as Region, BlockTypes.AIR!!.defaultState as Pattern)
            editSession.close()
            state = TemporaryWorldState.FREE
        }
    }

}