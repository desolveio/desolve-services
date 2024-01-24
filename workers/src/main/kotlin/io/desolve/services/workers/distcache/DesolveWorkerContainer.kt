package io.desolve.services.workers.distcache

import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import io.desolve.parser.ParsedProject
import io.desolve.services.distcache.DesolveDistcacheContainer
import io.desolve.services.protocol.TaskUpdateResponse
import io.desolve.services.protocol.WorkerRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
class DesolveWorkerContainer : DesolveDistcacheContainer()
{
    private val workerQueued = "desolve:workers:queued"
    private val workerArtifactMappings = "desolve:artifacts:mappings"
    private val workerReports = "desolve:workers:reports:"

    val reportSerializer = GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .create()

    fun publishArtifactIdMapping(
        dependency: ParsedProject,
        artifactId: String
    )
    {
        this.publishArtifactIdMapping(
            "${dependency.groupId}/${dependency.artifactId}/${dependency.version}",
            artifactId
        )
    }

    private fun publishArtifactIdMapping(
        rawArtifact: String,
        artifactId: String
    )
    {
        this.connection().async()
            .hset(
                this.workerArtifactMappings,
                rawArtifact, artifactId
            )
    }

    fun queuedForWork(
        artifactId: String
    ): Boolean
    {
        return this.connection().sync()
            .hget(
                this.workerQueued, artifactId
            ) != null
    }

    fun publishReport(
        report: TaskUpdateResponse
    )
    {
        val key = "$workerReports${report.artifactUniqueId}"

        this.connection().async()
            .set(
                key, reportSerializer.toJson(report)
            )

        this.connection().async()
            .expire(
                key, TimeUnit.MINUTES
                    .toSeconds(10L)
            )
    }

    fun getReport(
        artifactId: String
    ): TaskUpdateResponse?
    {
        return serializer()
            .decodeFromString(
                this.connection().sync()
                    .get(
                        "$workerReports$artifactId"
                    )
            )
    }

    fun removeFromGlobalQueue(
        artifactId: String
    )
    {
        this.connection().async()
            .hdel(
                this.workerQueued, artifactId
            )
    }

    fun addToGlobalQueue(
        request: WorkerRequest
    )
    {
        this.connection().async()
            .hset(
                this.workerQueued,
                request.artifactUniqueId,
                serializer().encodeToString(request)
            )
    }
}
