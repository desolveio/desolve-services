package io.desolve.services.profiles.subscription

import kotlinx.serialization.Contextual
import java.time.Instant
import kotlinx.serialization.Serializable

/**
 * @author GrowlyX
 * @since 6/6/2022
 */
@Serializable
data class DesolveUserSubscription(
    @Contextual
    val activationDate: Instant,
    @Contextual
    val expirationDate: Instant,
    val tier: DesolveUserSubscriptionTier
)
