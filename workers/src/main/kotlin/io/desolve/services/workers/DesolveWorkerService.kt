package io.desolve.services.workers

import io.desolve.services.distcache.DesolveDistcacheService
import io.desolve.services.protocol.*
import io.desolve.services.workers.distcache.DesolveWorkerContainer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles the reception of task update
 * requests & task start requests.
 *
 * Workers are synchronized through
 * our Redis cache.
 *
 * @author GrowlyX
 * @since 5/22/2022
 */
class DesolveWorkerService : WorkerGrpcKt.WorkerCoroutineImplBase(), KoinComponent
{
    private val server by inject<DesolveWorkerServer>()

    override suspend fun receiveTaskUpdate(
        request: TaskUpdateReqeust
    ): TaskUpdateResponse
    {
        val response =
            TaskUpdateResponse.newBuilder()
                .setArtifactUniqueId(request.artifactUniqueId)

        val container = DesolveDistcacheService
            .container<DesolveWorkerContainer>()

        val queued = container
            .queuedForWork(request.artifactUniqueId)

        if (queued)
        {
            return response
                .setStatus(QueueStatus.QUEUED)
                .build()
        }

        val report = container
            .getReport(request.artifactUniqueId)
            ?: return response
                .setStatus(QueueStatus.BUILDING)
                .build()

        return TaskUpdateResponse.newBuilder(report)
            .setStatus(QueueStatus.BUILT)
            .build()
    }

    override suspend fun startTaskWork(
        request: WorkerRequest
    ): WorkerReply
    {
        val response = WorkerReply.newBuilder()
            .setArtifactUniqueId(request.artifactUniqueId)

        val queued = DesolveDistcacheService
            .container<DesolveWorkerContainer>()
            .queuedForWork(request.artifactUniqueId)

        if (queued)
        {
            return response
                .setStatus(TaskQueueStatus.EXTERNALLY_QUEUED)
                .build()
        }

        DesolveWorkerQueue.queue(request)

        return response
            .setStatus(TaskQueueStatus.SUCCESS)
            .setWorkerUniqueId(
                this.server.workerUniqueId
            )
            .build()
    }
}
