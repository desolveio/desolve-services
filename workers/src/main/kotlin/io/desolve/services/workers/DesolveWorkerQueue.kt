package io.desolve.services.workers

import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import com.google.protobuf.ByteString
import io.desolve.parser.FileParseRecognition
import io.desolve.services.artifacts.assist.DesolveArtifactCompression
import io.desolve.services.artifacts.dao.DesolveStoredArtifact
import io.desolve.services.artifacts.dao.DesolveStoredArtifactService
import io.desolve.services.artifacts.dao.DesolveStoredProject
import io.desolve.services.artifacts.dao.DesolveStoredProjectService
import io.desolve.services.core.DesolveServiceLogger
import io.desolve.services.core.annotations.Configure
import io.desolve.services.core.parseUniqueId
import io.desolve.services.core.popOrNull
import io.desolve.services.core.suspend.SuspendingRunnable
import io.desolve.services.distcache.DesolveDistcacheService
import io.desolve.services.protocol.BuildStatus
import io.desolve.services.protocol.StowArtifactRequest
import io.desolve.services.protocol.TaskUpdateResponse
import io.desolve.services.protocol.WorkerRequest
import io.desolve.services.workers.distcache.DesolveWorkerContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.awt.Color
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Queues build tasks on this
 * single worker server instance.
 *
 * @author GrowlyX
 * @since 5/22/2022
 */
object DesolveWorkerQueue : SuspendingRunnable, KoinComponent
{
    private val queued =
        LinkedList<WorkerRequest>()

    private val server by inject<DesolveWorkerServer>()

    private val artifactService by inject<DesolveStoredArtifactService>()
    private val projectService by inject<DesolveStoredProjectService>()

    private val scope = CoroutineScope(Dispatchers.Default)

    @Configure
    fun configure()
    {
        val executor = Executors
            .newSingleThreadScheduledExecutor()

        executor.scheduleAtFixedRate(
            this, 0L,
            1L, TimeUnit.SECONDS
        )
    }

    fun queue(
        request: WorkerRequest
    )
    {
        this.queued.add(request)

        DesolveDistcacheService
            .container<DesolveWorkerContainer>()
            .addToGlobalQueue(request)
    }

    fun requestByArtifactId(
        artifactId: String
    ): WorkerRequest?
    {
        return this.queued
            .firstOrNull {
                it.artifactUniqueId == artifactId
            }
    }

    private fun submitReport(
        response: TaskUpdateResponse.Builder
    )
    {
        val report = response.build()

        DesolveDistcacheService
            .container<DesolveWorkerContainer>()
            .publishReport(report)
    }

    override suspend fun suspended()
    {
        val request = this.queued.popOrNull()
            ?: return

        DesolveDistcacheService
            .container<DesolveWorkerContainer>()
            .removeFromGlobalQueue(
                request.artifactUniqueId
            )

        val response = TaskUpdateResponse.newBuilder()
            .setArtifactUniqueId(request.artifactUniqueId)

        val start = System.currentTimeMillis()

        DesolveServiceLogger.logger()
            .info(
                "Attempting to build from \"${request.specification.repositoryUrl}\"..."
            )

        DesolveWorkerWebhook.post(
            WebhookEmbedBuilder()
                .setColor(Color.decode("#2f3136").rgb)
                .setTitle(
                    WebhookEmbed.EmbedTitle("Build Started", null)
                )
                .setDescription("Started build process for: ${request.specification.repositoryUrl}")
                .build()
        )

        val auth = request.specification.authentication

        val cloneSpec = FileParseRecognition
            .RepositoryCloneSpec(
                branch = request.specification.branch
                    ?.ifEmpty { "main" } ?: "main",
                credentials = request.specification
                    .hasAuthentication(),
                // TODO: find based on repositoryUrl
                credentialsProviderType = FileParseRecognition
                    .RepositoryCloneCredentialProviders.Basic,
                credentialsProvider = (auth?.username ?: "") to (auth?.password ?: "")
            )

        val project = FileParseRecognition
            .parseFromRepository(
                url = request.specification.repositoryUrl,
                spec = cloneSpec
            )
            .exceptionally {
                if (it.message == "Authentication is required but no CredentialsProvider has been registered")
                {
                    println("Attempted to build from a repository with no credentials setup!")
                    return@exceptionally null
                }

                it.printStackTrace()
                return@exceptionally null
            }
            .join()

        if (project?.first == null)
        {
            DesolveWorkerWebhook.post(
                WebhookEmbedBuilder()
                    .setColor(Color.decode("#ff4242").rgb)
                    .setTitle(
                        WebhookEmbed.EmbedTitle("Build Failed", null)
                    )
                    .setDescription("Failed to parse/build repository. Are credentials setup properly?")
                    .build()
            )

            response.buildStatus = BuildStatus.FAILED
            response.buildAdditionalInformation = "Failed to parse/build repository."

            this.submitReport(response)
            return
        }

        DesolveWorkerWebhook.post(
            WebhookEmbedBuilder()
                .setColor(Color.decode("#17ca48").rgb)
                .setTitle(
                    WebhookEmbed.EmbedTitle("Build Succeeded", null)
                )
                .setDescription(
                    "Completed build process for ${request.specification.repositoryUrl} in ${
                        (System.currentTimeMillis() - start) / 1000
                    } seconds!"
                )
                .build()
        )

        DesolveServiceLogger.logger()
            .info(
                "Project built successfully in ${
                    (System.currentTimeMillis() - start) / 1000
                } seconds!"
            )

        response.addAllBuildLog(project.first!!.result.logs)

        val generatedArtifact = project.first!!
            .generateDirectory()

        if (!generatedArtifact.exists())
        {
            response.buildStatus = BuildStatus.FAILED
            response.buildAdditionalInformation = "Generated artifact doesn't exist."

            this.submitReport(response)
            return
        }

        DesolveServiceLogger.logger()
            .info("Generated artifact by name: ${generatedArtifact.name}")

        response.buildStatus = BuildStatus.SUCCEEDED

        val compressed = DesolveArtifactCompression
            .compress(generatedArtifact)
            .exceptionally {
                it.printStackTrace()
                return@exceptionally null
            }
            .join()

        if (compressed == null)
        {
            generatedArtifact.deleteRecursively()

            if (project.second.exists())
                project.second.deleteRecursively()

            response.buildStatus = BuildStatus.FAILED
            response.buildAdditionalInformation = "Failed to compress file."

            DesolveServiceLogger.logger().warning(
                "[Debug] Failed to compress file, details above."
            )

            this.submitReport(response)
            return
        }

        val mappedToByteString = compressed
            .mapValues {
                ByteString.copyFrom(it.value)
            }

        val stowageRequest = StowArtifactRequest.newBuilder()
            .setArtifactUniqueId(request.artifactUniqueId)
            .putAllContent(mappedToByteString)
            .build()

        val stowageReport = server
            .artifactClient.stub()
            .stowArtifacts(
                stowageRequest
            )

        generatedArtifact.deleteRecursively()

        if (project.second.exists())
            project.second.deleteRecursively()

        DesolveServiceLogger.logger()
            .info(
                "Deleted locally cached artifacts and uploaded to artifact server ${stowageReport.serverUniqueId}."
            )

        val parsedUniqueId = request
            .artifactUniqueId.parseUniqueId()!!

        scope.launch {
            artifactService.update(
                DesolveStoredArtifact(
                    parsedUniqueId,
                    version = project.first!!.version,
                    branch = "main"
                )
            )

            DesolveServiceLogger.logger()
                .info(
                    "Persisted stored artifact DAO."
                )
        }

        scope.launch {
            val internal = projectService
                .byRepository(
                    request.specification.repositoryUrl
                )
                ?: DesolveStoredProject(
                    UUID.randomUUID(),
                    request.specification
                        .repositoryUrl.lowercase()
                )

            internal.associated
                .add(
                    parsedUniqueId.toString()
                )

            projectService.update(internal)

            DesolveServiceLogger.logger()
                .info(
                    "Persisted new artifact state to project ${internal.uniqueId}."
                )
        }

        DesolveDistcacheService
            .container<DesolveWorkerContainer>()
            .publishArtifactIdMapping(
                project.first!!, request.artifactUniqueId
            )

        this.submitReport(response)
    }
}
