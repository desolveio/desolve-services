@file:UseSerializers(
    DesolveInstantSerializer::class
)
package io.desolve.services.artifacts.dao

import io.desolve.services.store.DesolveDataModel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

/**
 * @author GrowlyX
 * @since 6/22/2022
 */
@Serializable
data class DesolveStoredProject(
    @Contextual
    override val _id: UUID,
    val repository: String,
    val associated: MutableList<@Contextual String> =
        mutableListOf()
) : DesolveDataModel<UUID>
{
    val uniqueId: UUID
        get() = _id
}
