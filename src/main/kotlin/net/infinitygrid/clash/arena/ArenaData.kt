package net.infinitygrid.clash.arena

data class SpawnPoint(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float
)

data class ItemSpawnPoint(
    val x: Double,
    val y: Double,
    val z: Double
)

data class ArenaData(
    val schematic: String,
    val mapName: String,
    val mapIcon: String,
    val mapCreators: List<String>,
    val description: String,
    val spectatorSpawn: SpawnPoint,
    val playerSpawns: List<SpawnPoint>,
    val itemSpawns: List<ItemSpawnPoint>
)
