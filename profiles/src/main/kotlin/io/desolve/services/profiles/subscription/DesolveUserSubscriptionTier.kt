package io.desolve.services.profiles.subscription

import java.time.Duration

/**
 * @author GrowlyX
 * @since 6/6/2022
 */
enum class DesolveUserSubscriptionTier(
    val perks: Map<String, String>,
    val price: Double,
    val purchasable: Boolean = true,
    val trialAvailable: Boolean = false,
    val trialDuration: Duration = Duration.ofDays(7L)
)
{
    Free(
        perks = mapOf(),
        price = 0.0
    ),
    Individual(
        perks = mapOf(
            DesolveUserSubscriptionPerks.PRIVATE_REPOSITORIES to "5",
            DesolveUserSubscriptionPerks.TRIGGER_EVENTS to "true"
        ),
        price = 6.5,
        trialAvailable = true
    ),
    Premium(
        perks = mapOf(
            DesolveUserSubscriptionPerks.PRIVATE_REPOSITORIES to "10",
            DesolveUserSubscriptionPerks.TRIGGER_EVENTS to "true",
            DesolveUserSubscriptionPerks.CUSTOM_DOMAIN to "true",
            DesolveUserSubscriptionPerks.SCHEDULED_BUILDS to "true"
        ),
        price = 12.0
    ),
    Professional(
        perks = mapOf(
            DesolveUserSubscriptionPerks.PRIVATE_REPOSITORIES to "50",
            DesolveUserSubscriptionPerks.TRIGGER_EVENTS to "true",
            DesolveUserSubscriptionPerks.CUSTOM_DOMAIN to "true",
            DesolveUserSubscriptionPerks.SCHEDULED_BUILDS to "true",
            DesolveUserSubscriptionPerks.API_ACCESS to "true"
        ),
        price = 25.0
    ),
    Enterprise(
        perks = mapOf(
            DesolveUserSubscriptionPerks.PRIVATE_REPOSITORIES to "200",
            DesolveUserSubscriptionPerks.TRIGGER_EVENTS to "true",
            DesolveUserSubscriptionPerks.CUSTOM_DOMAIN to "true",
            DesolveUserSubscriptionPerks.SCHEDULED_BUILDS to "true",
            DesolveUserSubscriptionPerks.API_ACCESS to "true"
        ),
        price = 75.0
    )
}
