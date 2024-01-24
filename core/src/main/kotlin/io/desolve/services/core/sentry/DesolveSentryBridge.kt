package io.desolve.services.core.sentry

import io.desolve.services.core.annotations.Configure
import io.sentry.Sentry
import org.koin.core.component.KoinComponent

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
// TODO: 5/25/2022 properly register component
object DesolveSentryBridge : KoinComponent
{
    @Configure
    fun configure()
    {
        val dsn = System
            .getenv("DESOLVE_SENTRY_DSN")
            ?: return

        Sentry.init { options ->
            options.dsn = dsn

            options.connectionTimeoutMillis = 10000
            options.readTimeoutMillis = 10000

            options.environment = "production"
        }
    }
}
