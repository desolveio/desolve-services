package io.desolve.services.artifacts.distcache

import io.desolve.services.artifacts.DesolveArtifactsServer
import io.desolve.services.core.annotations.Configure
import io.desolve.services.distcache.DesolveDistcacheService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
object DesolveArtifactDistcacheService : KoinComponent
{
    val server by inject<DesolveArtifactsServer>()

    @Configure
    fun configure()
    {
        val container =
            DesolveArtifactContainer(server)

        DesolveDistcacheService
            .container(container)
    }
}
