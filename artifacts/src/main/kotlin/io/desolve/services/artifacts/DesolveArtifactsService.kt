package io.desolve.services.artifacts

import com.google.protobuf.ByteString
import io.desolve.config.impl.EnvTableRepositoryConfig
import io.desolve.services.artifacts.assist.DesolveArtifactCompression
import io.desolve.services.artifacts.distcache.DesolveArtifactContainer
import io.desolve.services.core.DesolveServiceLogger
import io.desolve.services.distcache.DesolveDistcacheService
import io.desolve.services.protocol.ArtifactLookupReply
import io.desolve.services.protocol.ArtifactLookupRequest
import io.desolve.services.protocol.ArtifactLookupResult
import io.desolve.services.protocol.StowArtifactReply
import io.desolve.services.protocol.StowArtifactRequest
import io.desolve.services.protocol.StowageGrpcKt
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.nio.file.Files

/**
 * Stores & lookups artifacts
 * within this server.
 *
 * Artifact contents are serialized in a
 * [Map] <String, ByteString> form, with the key
 * being the location of the file within context
 * to the root directory, and the value being
 * the deflated/inflated byte array.
 *
 * @author GrowlyX
 * @since 5/23/2022
 */
class DesolveArtifactsService : StowageGrpcKt.StowageCoroutineImplBase(), KoinComponent
{
    private val server by inject<DesolveArtifactsServer>()

    private val artifactDirectory =
        File(
            EnvTableRepositoryConfig
                .getDirectory(),
            "artifacts"
        ).apply {
            if (!exists())
                mkdirs()
        }

    init
    {
        DesolveServiceLogger.logger().info(
            "[Artifacts] Stowage ID: ${server.stowageId}"
        )
    }

    override suspend fun stowArtifacts(
        request: StowArtifactRequest
    ): StowArtifactReply
    {
        val reply = StowArtifactReply
            .newBuilder()
            .setServerUniqueId(this.server.stowageId)

        // TODO: keep in compressed form for long-term storage?
        val inflated = DesolveArtifactCompression
            .decompress(request.contentMap)
            .exceptionally {
                it.printStackTrace()
                return@exceptionally null
            }
            .join()

        if (inflated == null)
        {
            DesolveServiceLogger.logger().severe(
                "[Debug] Failed to inflate package, details above."
            )
            return reply.build()
        }

        val subDirectory = File(
            this.artifactDirectory, request.artifactUniqueId
        )

        subDirectory.mkdirs()

        for (entry in inflated.entries)
        {
            val file = File(
                subDirectory, entry.key
            )

            DesolveServiceLogger.logger().info(
                "[Debug] Created new file: ${file.absolutePath}"
            )

            kotlin.runCatching {
                file.createNewFile()

                Files.write(
                    file.toPath(), entry.value
                )
            }.onFailure {
                it.printStackTrace()
            }
        }

        DesolveDistcacheService
            .container<DesolveArtifactContainer>()
            .publishArtifactLocation(
                request.artifactUniqueId
            )

        return reply.build()
    }

    override suspend fun lookupArtifact(
        request: ArtifactLookupRequest
    ): ArtifactLookupReply
    {
        val subDirectory = File(
            this.artifactDirectory, request.artifactUniqueId
        )

        val reply = ArtifactLookupReply
            .newBuilder()

        if (!subDirectory.exists())
        {
            return reply
                .setResult(
                    ArtifactLookupResult.NOT_FOUND
                )
                .build()
        }

        val compressed = DesolveArtifactCompression
            .compress(subDirectory).join()

        val mappedToByteString = compressed
            .mapValues {
                ByteString.copyFrom(it.value)
            }

        return reply
            .setResult(ArtifactLookupResult.EXISTS)
            .putAllContent(mappedToByteString)
            .build()
    }
}
