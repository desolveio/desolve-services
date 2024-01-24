package io.desolve.services.workers

import io.desolve.services.artifacts.dao.DesolveStoredArtifactService
import io.desolve.services.artifacts.dao.DesolveStoredProjectService
import io.desolve.services.workers.distcache.DesolveWorkerDistcacheService
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * @author GrowlyX
 * @since 5/25/2022
 */
@Module
class DesolveWorkerModule
{
    private val artifactService = DesolveStoredArtifactService()
    private val projectService = DesolveStoredProjectService()

    @Single
    fun server() = DesolveWorkerBootstrap.server

    @Single
    fun queue() = DesolveWorkerQueue

    @Single
    fun distCache() = DesolveWorkerDistcacheService

    @Single
    fun artifacts() = DesolveWorkerService()

    @Single
    fun webhooks() = DesolveWorkerWebhook

    @Single
    fun service() = artifactService

    @Single
    fun project() = projectService
}
