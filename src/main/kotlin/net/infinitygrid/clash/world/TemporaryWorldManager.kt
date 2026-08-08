package net.infinitygrid.clash.world

import net.infinitygrid.clash.CLASH
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
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
        initializePool(preInitialize)
    }

    private fun registerWorld(world: World) {
        createdWorlds.add(TemporaryWorld(world))
    }

    private fun initializePool(count: Int) {
        val keys = List(count) { NamespacedKey("clash", UUID.randomUUID().toString()) }

        val copyFutures = keys.map { copyVoidWorld(it) }

        CompletableFuture.allOf(*copyFutures.toTypedArray()).thenRun {
            Bukkit.getScheduler().runTask(CLASH.INSTANCE, Runnable {
                keys.forEach { createWorldFromKey(it) }
            })
        }
    }

    private fun createWorldFromKey(key: NamespacedKey): World {
        val bukkitWorldCreator = WorldCreator.ofKey(key)
            .generator(EmptyWorldGenerator())

        val world = Bukkit.createWorld(bukkitWorldCreator)!!
        world.setGameRuleValue("spawnChunkRadius", "0")
        registerWorld(world)
        return world
    }

    fun getAnyFreeWorld(): TemporaryWorld? {
        return createdWorlds.find { it.state == TemporaryWorldState.FREE }
    }

    private fun cleanStartup() {
        val dimensionsFolder = File(Bukkit.getWorldContainer(), "main/dimensions/clash")
        dimensionsFolder.listFiles()?.forEach { file ->
            if (file.isDirectory) FileUtils.deleteDirectory(file)
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

    private fun copyVoidWorld(key: NamespacedKey): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        Bukkit.getAsyncScheduler().runNow(CLASH.INSTANCE) {
            try {
                copyVoidWorldFiles(key)
                future.complete(true)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    private fun copyVoidWorldFiles(key: NamespacedKey) {
        val source = templateWorlds.resolve("void_template")
        if (!source.exists()) {
            throw IllegalStateException("Template world 'void_template' does not exist")
        }

        val target = File(Bukkit.getWorldContainer(), "main/dimensions/${key.namespace}/${key.key}")
        FileUtils.copyDirectory(source, target)
    }


}