package io.desolve.services.core.heartbeat

import io.desolve.services.core.AbstractDesolveServiceProvider
import io.desolve.services.core.suspend.SuspendingRunnable
import io.desolve.services.discovery.DesolveDiscoveryClient
import java.io.Closeable

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
class DesolveServiceHeartbeat(
    provider: AbstractDesolveServiceProvider<*>
) : SuspendingRunnable, Closeable
{
    private val registration = DesolveDiscoveryClient
        .register(
            provider.workerUniqueId,
            provider.provider.serviceId,
            provider.provider.serviceId,
            // TODO: 6/12/2022 use k8s assigned port
            provider.meta.port
        )

    override suspend fun suspended()
    {
        kotlin.runCatching {
            DesolveDiscoveryClient
                .discovery().agentClient()
                .pass(registration.id)
        }.onFailure {
            it.printStackTrace()
        }
    }

    override fun close()
    {
        kotlin.runCatching {
            DesolveDiscoveryClient
                .discovery().agentClient()
                .deregister(registration.id)
        }.onFailure {
            it.printStackTrace()
        }
    }
}
