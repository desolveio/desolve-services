package io.desolve.services.artifacts

import io.desolve.services.artifacts.distcache.DesolveArtifactDistcacheService
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * @author GrowlyX
 * @since 5/25/2022
 */
@Module
class DesolveArtifactsModule
{
    @Single
    fun server() = DesolveArtifactsBootstrap.server

    @Single
    fun distCache() = DesolveArtifactDistcacheService

    @Single
    fun artifacts() = DesolveArtifactsService()
}
