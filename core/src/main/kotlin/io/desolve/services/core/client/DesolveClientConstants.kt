package io.desolve.services.core.client

import io.desolve.services.core.DesolveServiceMeta
import io.desolve.services.core.client.resolver.MultiAddressNameResolverFactory
import io.desolve.services.discovery.resolve.DesolveConsulNameResolverProvider
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
object DesolveClientConstants
{
    private val resolvers = mutableMapOf<DesolveServiceMeta, Lazy<DesolveConsulNameResolverProvider>>()
        .apply {
            DesolveServiceMeta.SERVICEABLE.forEach {
                this[it] = lazy {
                    DesolveConsulNameResolverProvider(it.consul.lowercase(), 1)
                }
            }
        }

    private val MAX_INBOUND_MESSAGE_SIZE =
        System.getenv("DESOLVE_MAX_MESSAGE_SIZE")
            ?.toIntOrNull() ?: 26214400

    @JvmStatic
    fun build(meta: DesolveServiceMeta): ManagedChannel
    {
        return ManagedChannelBuilder.forTarget("service")
            .defaultLoadBalancingPolicy("round_robin")
            .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
            .usePlaintext()
            .nameResolverFactory(
                this.resolvers[meta]!!.value
            )
            .build()
    }

    @JvmStatic
    fun buildLegacy(resolver: MultiAddressNameResolverFactory): ManagedChannel
    {
        return ManagedChannelBuilder.forTarget("service")
            .defaultLoadBalancingPolicy("round_robin")
            .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
            .nameResolverFactory(resolver)
            .usePlaintext()
            .build()
    }
}
