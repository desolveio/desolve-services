package io.desolve.services.workers.distcache

import io.desolve.services.core.annotations.Configure
import io.desolve.services.distcache.DesolveDistcacheService
import org.koin.core.component.KoinComponent

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
object DesolveWorkerDistcacheService : KoinComponent
{
    @Configure
    fun configure()
    {
        DesolveDistcacheService
            .container(
                DesolveWorkerContainer()
            )
    }
}
