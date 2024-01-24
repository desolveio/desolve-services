package io.desolve.services.core.client

import io.grpc.ManagedChannel
import java.io.Closeable
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
class DesolveClientService<T>(
    private val channel: ManagedChannel,
    private val stub: T
) : Closeable
{
    fun stub() = this.stub

    override fun close()
    {
        this.channel.shutdown()
            .awaitTermination(5, TimeUnit.SECONDS)
    }
}
