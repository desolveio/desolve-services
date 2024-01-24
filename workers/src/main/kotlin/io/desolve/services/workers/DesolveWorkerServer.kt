package io.desolve.services.workers

import io.desolve.services.core.AbstractDesolveServiceProvider
import io.desolve.services.core.DesolveServiceMeta
import io.desolve.services.core.client.DesolveClientConstants
import io.desolve.services.core.client.DesolveClientService
import io.desolve.services.protocol.StowageGrpcKt
import io.desolve.services.protocol.WorkerGrpcKt
import org.koin.ksp.generated.module
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 5/22/2022
 */
class DesolveWorkerServer(
    meta: DesolveServiceMeta,
    args: Array<String>
) : AbstractDesolveServiceProvider<WorkerGrpcKt.WorkerCoroutineImplBase>(
    args, meta, meta
)
{
    private val artifactChannel = DesolveClientConstants
        .build(DesolveServiceMeta.Artifacts)

    override val coroutineBaseClass: KClass<WorkerGrpcKt.WorkerCoroutineImplBase> =
        WorkerGrpcKt.WorkerCoroutineImplBase::class

    val artifactClient =
        DesolveClientService(
            this.artifactChannel,
            StowageGrpcKt
                .StowageCoroutineStub(
                    this.artifactChannel
                )
        )

    override fun modules() =
        listOf(
            DesolveWorkerModule().module
        )
}
