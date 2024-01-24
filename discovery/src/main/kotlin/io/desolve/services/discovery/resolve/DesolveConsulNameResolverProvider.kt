package io.desolve.services.discovery.resolve

import io.grpc.NameResolver
import io.grpc.NameResolverProvider
import java.net.URI

/**
 * @author GrowlyX
 * @since 6/12/2022
 */
class DesolveConsulNameResolverProvider(
    private val serviceName: String,
    private val pauseInSeconds: Int
) : NameResolverProvider()
{
    override fun newNameResolver(uri: URI, args: NameResolver.Args) =
        DesolveConsulNameResolver(uri, serviceName, pauseInSeconds)

    override fun getDefaultScheme() = "consul"

    override fun isAvailable() = true
    override fun priority() = 10
}
