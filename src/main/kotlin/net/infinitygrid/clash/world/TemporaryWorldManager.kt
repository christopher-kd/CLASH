package net.infinitygrid.clash.world

import net.infinitygrid.clash.CLASH
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture

class TemporaryWorldManager(preInitialize: Int) {

    private val templateWorlds = CLASH.INSTANCE.fileManager.worldTemplatesFolder
    var createdWorlds = mutableListOf<TemporaryWorld>()
        private set

    init {
        cleanStartup()
        for (i in 0 until preInitialize) {
            createWorld("temp_${UUID.randomUUID()}")
        }
    }

    private fun registerWorld(world: World) {
        createdWorlds.add(TemporaryWorld(world))
    }

    fun createWorld(worldName: String): CompletableFuture<World> {
        val future = CompletableFuture<World>()

        val bukkitWorldCreator = WorldCreator(worldName)
            .generator(EmptyWorldGenerator())

        Bukkit.getAsyncScheduler().run {
            copyVoidWorld(worldName).thenAccept {

                Bukkit.getScheduler().run {
                    val world = Bukkit.createWorld(bukkitWorldCreator)!!
                    registerWorld(world)
                    future.complete(world)
                }

            }
        }

        return future
    }

    fun getAnyFreeWorld(): TemporaryWorld? {
        return createdWorlds.find { it.state == TemporaryWorldState.FREE }
    }

    private fun cleanStartup() {
        Bukkit.getWorldContainer().listFiles()?.forEach { file ->
            if (file.isDirectory && file.name.startsWith("temp_")) FileUtils.deleteDirectory(file)
        }
    }

    fun clean() {
        createdWorlds.forEach { world ->
            deleteWorld(world.bukkitWorld)
        }
    }

    private fun deleteWorld(world: World) {
        world.players.forEach { it.teleport(Bukkit.getWorlds()[0].spawnLocation) }
        Bukkit.unloadWorld(world, false)
            try {
                FileUtils.deleteDirectory(world.worldPath.toFile())
            } catch (e: Exception) {
                println(e)
            }
    }

    private fun copyVoidWorld(name: String): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        if (!templateWorlds.resolve("void_template").exists()) {
            future.completeExceptionally(IllegalStateException("Template world 'void_template' does not exist"))
        }

        val source = templateWorlds.resolve("void_template")
        val target = File(Bukkit.getWorldContainer(), name)

        FileUtils.copyDirectory(source, target)
        future.complete(true)
        return future
    }


}