package io.desolve.services.repository

import io.desolve.services.artifacts.assist.DesolveArtifactCompression
import io.desolve.services.artifacts.dao.DesolveStoredArtifactService
import io.desolve.services.core.client.DesolveClientService
import io.desolve.services.distcache.DesolveDistcacheService
import io.desolve.services.protocol.ArtifactLookupRequest
import io.desolve.services.protocol.DependencyLocation
import io.desolve.services.protocol.StowageGrpcKt
import io.desolve.services.repository.distcache.DesolveRepositoryContainer
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.defaultForFileExtension
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

/**
 * @author GrowlyX
 * @since 5/24/2022
 */
class DesolveRepositoryRouting(
    private val artifactService: DesolveClientService<StowageGrpcKt.StowageCoroutineStub>
)
{
    private val service = DesolveStoredArtifactService()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun route(route: Route)
    {
        val iconType = ContentType
            .parse("image/x-icon")

        val icon = this::class.java
            .getResourceAsStream("desolve_squircle.png")
            ?.readBytes()

        if (icon != null)
        {
            route.get("/favicon.ico") {
                this.context.respondBytes(
                    contentType = iconType,
                    bytes = icon
                )
            }
        }

        route.get("/repository/{repository...}") {
            val parsed = parseFile(this.context)
                ?: return@get

            val contentType = ContentType
                .defaultForFileExtension(
                    parsed.first.fileId
                        .split(".")
                        .lastOrNull() ?: ""
                )

            this.context.respondBytes(
                bytes = parsed.second,
                contentType = contentType
            )
        }
    }

    private suspend fun parseFile(call: ApplicationCall): Pair<DependencyLocation, ByteArray>?
    {
        val entries = call.parameters
            .getAll("repository")

        if (entries.isNullOrEmpty())
        {
            call.respond(HttpStatusCode.OK, mapOf(
                "status" to "operational",
                "message" to "All assets should be available at their dedicated paths."
            ))
            return null
        }

        val size = entries.size

        val parameterAmount = 4
        val groupIdLength = size - parameterAmount + 1

        var groupId = ""
        val artifactId = entries.getOrNull(groupIdLength)
        val version = entries.getOrNull(groupIdLength + 1)
        val fileName = entries.getOrNull(groupIdLength + 2)

        for (i in 0 until groupIdLength)
        {
            groupId += entries.getOrNull(i)

            if (i != groupIdLength - 1)
            {
                groupId += "."
            }
        }

        if (
            artifactId == null || version == null || fileName == null
        )
        {
            call.respond(
                HttpStatusCode.NotFound,
                "Invalid parameter format provided: ${groupId}:${artifactId}:${version}, $fileName"
            )
            return null
        }

        val container = DesolveDistcacheService
            .container<DesolveRepositoryContainer>()

        val data = DependencyLocation
            .newBuilder()
            .setGroupId(groupId)
            .setArtifactId(artifactId)
            .setVersion(version)
            .setFileId(fileName)
            .build()

        val internalArtifactId =
            container.findArtifactId(data)

        if (internalArtifactId == null)
        {
            call.respond(
                HttpStatusCode.NotFound,
                "No artifact by the triple (${groupId}:${artifactId}:${version}) exists in our servers."
            )
            return null
        }

        val artifactLocation =
            container.findArtifactLocation(
                internalArtifactId
            )

        val lookupRequest = ArtifactLookupRequest
            .newBuilder()
            .setArtifactUniqueId(internalArtifactId)
            // TODO: 5/24/2022 target this artifact
            //  server specifically.
            .setArtifactServerId(artifactLocation)
            .build()

        val content = this
            .artifactService.stub()
            .lookupArtifact(
                lookupRequest
            )

        val bytes = content.contentMap[data.fileId]

        // the key was wrong, meaning it was either
        // a directory, or an invalid file.
        if (bytes == null)
        {
            call.respond(
                HttpStatusCode.NotFound,
                "No proper file found with provided arguments.\n${data}"
            )
            return null
        }

        val downloadDate = Instant.now()

        scope.launch {
            val stored = service
                .findByUniqueId(
                    UUID.fromString(internalArtifactId)
                )
                ?: return@launch

            if (stored.metrics.firstDownloaded == null)
                stored.metrics.firstDownloaded = downloadDate

            stored.metrics.lastDownloaded = downloadDate
            stored.metrics.downloads.add(downloadDate)

            service.update(stored)
        }

        return Pair(
            data, DesolveArtifactCompression
                .inflateByteArray(
                    bytes.toByteArray()
                )
        )
    }
}
