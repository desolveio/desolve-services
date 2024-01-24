package io.desolve.services.core.client.resolver

import io.grpc.Attributes
import io.grpc.EquivalentAddressGroup
import io.grpc.NameResolver
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.URI

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
@Deprecated("Use DesolveConsulNameResolverProvider")
class MultiAddressNameResolverFactory(
    vararg address: SocketAddress
) : NameResolver.Factory()
{
    constructor(
        vararg address: Pair<String, Int>
    ) : this(
        *address.map {
            InetSocketAddress(it.first, it.second)
        }.toTypedArray()
    )

    val addresses = address
        .map { EquivalentAddressGroup(it) }

    override fun getDefaultScheme() =
        "multiaddress"

    override fun newNameResolver(
        notUsedUri: URI?,
        args: NameResolver.Args?
    ): NameResolver
    {
        return object : NameResolver()
        {
            override fun getServiceAuthority() =
                "fakeAuthority"

            override fun start(
                listener: Listener2
            )
            {
                listener.onResult(
                    ResolutionResult.newBuilder()
                        .setAddresses(this@MultiAddressNameResolverFactory.addresses)
                        .setAttributes(Attributes.EMPTY)
                        .build()
                )
            }

            override fun shutdown()
            {

            }
        }
    }
}
