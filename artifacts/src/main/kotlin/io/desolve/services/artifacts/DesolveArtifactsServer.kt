package io.desolve.services.artifacts

import io.desolve.services.core.AbstractDesolveServiceProvider
import io.desolve.services.core.DesolveServiceMeta
import io.desolve.services.protocol.StowageGrpcKt
import org.koin.ksp.generated.module
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 5/22/2022
 */
class DesolveArtifactsServer(
    meta: DesolveServiceMeta,
    val stowageId: String,
    args: Array<String>
) : AbstractDesolveServiceProvider<StowageGrpcKt.StowageCoroutineImplBase>(
    args, meta, meta
)
{
    override val coroutineBaseClass: KClass<StowageGrpcKt.StowageCoroutineImplBase> =
        StowageGrpcKt.StowageCoroutineImplBase::class

    override fun modules() =
        listOf(
            DesolveArtifactsModule().module
        )
}
