package net.infinitygrid.clash.world

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.*

class EmptyWorldGenerator : ChunkGenerator() {

    override fun generateNoise(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) {
        // Do nothing
    }

    override fun getFixedSpawnLocation(world: World, random: Random): Location {
        return Location(world, 0.0, 150.0, 0.0)
    }

}