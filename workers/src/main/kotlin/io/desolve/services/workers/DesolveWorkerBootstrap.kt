package io.desolve.services.workers

import io.desolve.services.core.DesolveServiceMeta

/**
 * @author GrowlyX
 * @since 5/22/2022
 */
object DesolveWorkerBootstrap
{
    lateinit var server: DesolveWorkerServer

    @JvmStatic
    fun main(args: Array<String>)
    {
        val server = DesolveWorkerServer(
            DesolveServiceMeta.Workers, args
        )

        Runtime.getRuntime().addShutdownHook(
            Thread {
                server.artifactClient.close()
            }
        )

        this.server = server
        this.server.start()
    }
}
