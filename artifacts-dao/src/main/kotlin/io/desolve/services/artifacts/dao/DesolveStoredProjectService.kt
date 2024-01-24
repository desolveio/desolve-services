package io.desolve.services.artifacts.dao

import io.desolve.services.store.DesolveDataStore
import org.litote.kmongo.eq
import java.util.*

/**
 * @author GrowlyX
 * @since 6/16/2022
 */
class DesolveStoredProjectService : DesolveDataStore<DesolveStoredProject, UUID>()
{
    suspend fun byRepository(repository: String) = this.desolveModelCollection
        .findOne(DesolveStoredProject::repository eq repository.lowercase())

    override fun getType() = DesolveStoredProject::class
}
