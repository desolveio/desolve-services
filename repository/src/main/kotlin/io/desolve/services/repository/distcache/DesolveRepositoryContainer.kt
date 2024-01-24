package io.desolve.services.repository.distcache

import io.desolve.services.distcache.DesolveDistcacheContainer
import io.desolve.services.protocol.DependencyLocation

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
class DesolveRepositoryContainer : DesolveDistcacheContainer()
{
    private val artifactMappings =
        "desolve:artifacts:mappings"

    fun findArtifactId(
        dependency: DependencyLocation
    ): String?
    {
        return this.findArtifactId(
            "${dependency.groupId}/${dependency.artifactId}/${dependency.version}"
        )
    }

    fun findArtifactId(
        rawArtifact: String
    ): String?
    {
        return this.connection().sync()
            .hget(
                this.artifactMappings, rawArtifact
            )
    }

    fun publishArtifactIdMapping(
        dependency: DependencyLocation,
        artifactId: String
    )
    {
        this.publishArtifactIdMapping(
            "${dependency.groupId}/${dependency.artifactId}/${dependency.version}",
            artifactId
        )
    }

    fun publishArtifactIdMapping(
        rawArtifact: String,
        artifactId: String
    )
    {
        this.connection().async()
            .hset(
                this.artifactMappings,
                rawArtifact, artifactId
            )
    }

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
}
