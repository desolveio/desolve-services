package io.desolve.services.artifacts.distcache

import io.desolve.services.artifacts.DesolveArtifactsServer
import io.desolve.services.distcache.DesolveDistcacheContainer

/**
 * Useful methods to find a location of an
 * artifact referred to by its artifactId.
 *
 * @author GrowlyX
 * @since 5/23/2022
 */
class DesolveArtifactContainer(
    private val server: DesolveArtifactsServer
) : DesolveDistcacheContainer()
{
    private val artifactLocations =
        "desolve:artifacts:locations"

    fun findArtifactLocation(
        artifactId: String
    ): String?
    {
        return this.connection().sync()
            .hget(
                this.artifactLocations, artifactId
            )
    }

    fun publishArtifactLocation(
        artifactId: String
    )
    {
        this.connection().async()
            .hset(
                this.artifactLocations,
                artifactId, server.stowageId
            )
    }
}
