package io.desolve.services.profiles

import kotlinx.serialization.Serializable

/**
 * @author GrowlyX
 * @since 6/6/2022
 */
@Serializable
data class DesolveUserProfileAssets(
    var profilePictureUrl: String? = null,
    var gitHubUserId: Int = -1
)
