package io.desolve.services.discovery

import com.google.common.net.HostAndPort
import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import com.orbitz.consul.model.agent.Registration
import io.desolve.services.containers.DesolveContainerHelper

/**
 * @author GrowlyX
 * @since 6/12/2022
 */
object DesolveDiscoveryClient
{
    private val consul = Consul
        .builder()
        .withHostAndPort(
            HostAndPort.fromParts(
                DesolveContainerHelper.address(), 8500
            )
        )
        .withReadTimeoutMillis(1000L)
        .build()!!

    fun discovery() = this.consul

    fun register(
        serviceId: String,
        serviceName: String,
        serviceType: String,
        port: Int
    ): Registration
    {
        val registration = ImmutableRegistration
            .builder()
            .id(serviceId)
            .name(serviceName)
            .check(Registration.RegCheck.ttl(2L))
            .meta(mapOf(
                "type" to serviceType,
                "docker" to DesolveContainerHelper
                    .containerized().toString()
            ))
            .address(
                DesolveContainerHelper.address()
            )
            .port(port)
            .build()

        this.consul.agentClient()
            .register(registration)

        return registration
    }
}
