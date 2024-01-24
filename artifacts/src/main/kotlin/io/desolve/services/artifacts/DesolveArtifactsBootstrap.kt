package io.desolve.services.artifacts

import io.desolve.services.core.DesolveServiceMeta

/**
 * @author GrowlyX
 * @since 5/22/2022
 */
object DesolveArtifactsBootstrap
{
    lateinit var server: DesolveArtifactsServer

    @JvmStatic
    fun main(args: Array<String>)
    {
        val server = DesolveArtifactsServer(
            DesolveServiceMeta.Artifacts,
            args.firstOrNull() ?: "aft-DEV", args
        )

        this.server = server
        this.server.start()
    }
}
