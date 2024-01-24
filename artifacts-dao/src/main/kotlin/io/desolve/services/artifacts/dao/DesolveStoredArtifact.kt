@file:UseSerializers(
    DesolveInstantSerializer::class
)
package io.desolve.services.artifacts.dao

import io.desolve.services.store.DesolveDataModel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant
import java.util.UUID

/**
 * Artifact IDs are represented
 * as [UUID]s.
 *
 * @author GrowlyX
 * @since 6/16/2022
 */
@Serializable
data class DesolveStoredArtifact(
    @Contextual
    override val _id: UUID,
    @Contextual
    val owner: UUID? = null,
    val metrics: DesolveStoredArtifactMetrics = DesolveStoredArtifactMetrics(),
    val created: Instant = Instant.now(),
    val accessibleBy: MutableList<@Contextual UUID> = mutableListOf(),
    val metadata: MutableMap<String, String> = mutableMapOf(),
    val version: String,
    val branch: String
) : DesolveDataModel<UUID>
