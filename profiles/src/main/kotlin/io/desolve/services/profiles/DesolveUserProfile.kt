package io.desolve.services.profiles

import io.desolve.services.profiles.subscription.DesolveUserSubscription
import io.desolve.services.profiles.subscription.DesolveUserSubscriptionTier
import io.desolve.services.store.DesolveDataModel
import io.ktor.server.auth.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

/**
 * @author GrowlyX
 * @since 5/24/2022
 */
@Serializable
data class DesolveUserProfile(
    @Contextual
    val uniqueId: UUID,
    @Contextual
    override val _id: UUID = uniqueId,
    var username: String,
    var password: String,
    var email: String,
    @Contextual
    var refreshToken: DesolveUserProfileToken? = null,
    @Contextual
    val assets: DesolveUserProfileAssets =
        DesolveUserProfileAssets(),
    val subscriptions: MutableList<@Contextual DesolveUserSubscription> = mutableListOf()
) : Principal, DesolveDataModel<UUID>
{
    fun isSubscriptionActive(
        tier: DesolveUserSubscriptionTier
    ) = subscriptions.any {
        it.tier == tier
    }

    fun isPerkAccessible(
        perk: String
    ) = subscriptions.any {
        it.tier.perks[perk] != null
    }
}
