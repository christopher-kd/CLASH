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
import org.bukkit.Bukkit
import org.bukkit.World
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

    fun pasteSchematicAsync(schematicName: String): CompletableFuture<Unit> {
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
            } catch (e: Exception) {
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