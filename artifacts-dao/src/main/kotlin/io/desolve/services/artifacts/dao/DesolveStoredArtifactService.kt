package io.desolve.services.artifacts.dao

import io.desolve.services.store.DesolveDataStore
import org.litote.kmongo.eq
import java.util.*

/**
 * @author GrowlyX
 * @since 6/16/2022
 */
class DesolveStoredArtifactService : DesolveDataStore<DesolveStoredArtifact, UUID>()
{
    fun ownedBy(owner: UUID) = this.desolveModelCollection
        .find(DesolveStoredArtifact::owner eq owner)

    override fun getType() = DesolveStoredArtifact::class
}
