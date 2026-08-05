package net.infinitygrid.clash.arena

import net.infinitygrid.clash.CLASH
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class CreatorNameCache {

    private data class MinetoolsProfile(val name: String?)

    private val names = ConcurrentHashMap<UUID, String>()
    private val httpClient = HttpClient.newHttpClient()

    fun get(uuid: UUID): String? = names[uuid]

    fun loadAll(uuids: Collection<UUID>): CompletableFuture<Void> {
        val requests = uuids.distinct().map { fetchName(it) }
        return CompletableFuture.allOf(*requests.toTypedArray())
    }

    private fun fetchName(uuid: UUID): CompletableFuture<Void> {
        val trimmed = uuid.toString().replace("-", "")
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.minetools.eu/uuid/$trimmed"))
            .GET()
            .build()

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept { response ->
                if (response.statusCode() != 200) return@thenAccept
                runCatching {
                    val profile = CLASH.INSTANCE.fileManager.gson.fromJson(response.body(), MinetoolsProfile::class.java)
                    profile?.name?.let { names[uuid] = it }
                }.onFailure { ex ->
                    CLASH.INSTANCE.logger.warning("Failed to parse minetools response for $uuid: ${ex.message}")
                }
            }
    }
}
