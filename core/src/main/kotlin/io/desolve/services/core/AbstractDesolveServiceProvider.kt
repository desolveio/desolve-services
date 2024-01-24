package io.desolve.services.core

import io.desolve.services.containers.DesolveContainerHelper
import io.desolve.services.containers.mappings.DockerInspectModel
import io.desolve.services.containers.mappings.inspectModel
import io.desolve.services.core.annotations.Configure
import io.desolve.services.core.client.DesolveClientConstants
import io.desolve.services.core.client.DesolveClientService
import io.desolve.services.core.client.resolver.MultiAddressNameResolverFactory
import io.desolve.services.core.heartbeat.DesolveServiceHeartbeat
import io.desolve.services.distcache.DesolveDistcacheService
import io.desolve.services.protocol.ContainerGrpcKt
import io.desolve.services.protocol.ContainerRequest
import io.grpc.BindableService
import io.grpc.Server
import io.grpc.kotlin.AbstractCoroutineServerImpl
import io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.HealthStatusManager
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 5/22/2022
 */
abstract class AbstractDesolveServiceProvider<T : AbstractCoroutineServerImpl>(
    private val args: Array<String>,
    val meta: DesolveServiceMeta,
    val provider: DesolveServiceProvider,
    var port: Int = args.firstOrNull()?.toInt() ?: 8080
)
{
    private val workerId: UUID = UUID.randomUUID()

    val workerUniqueId = "${provider.serviceId}-${
        workerId.toString().split("-")[0]
    }"

    private val serviceLocator =
        DesolveServiceLocator(
            this::class, "io.desolve.services.${
                this.meta.name.lowercase()
            }"
        )

    private val serviceInjector by lazy {
        startKoin {
            printLogger()
        }
    }

    private var inspection: DockerInspectModel? = null
    var server: Server? = null

    abstract val coroutineBaseClass: KClass<T>

    abstract fun modules(): List<Module>

    fun start()
    {
        val millis = System.currentTimeMillis()

        DesolveServiceShutdown.configure()
        DesolveServiceLogger.logger()
            .info("[Desolve] ${provider.serviceId.capitalize()} starting on port $port...")

        this.serviceInjector.modules(this.modules())

        this.serviceLocator
            .getSubTypes<KoinComponent>()
            .forEach {
                val configure = it.methods
                    .firstOrNull { method ->
                        method
                            .isAnnotationPresent(
                                Configure::class.java
                            )
                    }
                    ?: return@forEach

                it.kotlin.objectInstance
                    ?.apply {
                        configure.invoke(this)
                    }
            }

        val services = this.serviceLocator
            .getSubTypes(this.coroutineBaseClass)

        val server = NettyServerBuilder
            .forAddress(
                InetSocketAddress(
                    "0.0.0.0",
                    this.port
                )
            )
            .flowControlWindow(1048576)
            .maxInboundMetadataSize(8192 * 2)
            .maxInboundMessageSize(26214400)
            .addService(
                HealthStatusManager().healthService
            )
            .apply {
                for (service in services)
                {
                    val bindable = serviceInjector
                        .koin.get(service.kotlin) as BindableService

                    DesolveServiceLogger.logger()
                        .info(
                            "[Desolve] [Service] Lazily loaded service: ${
                                bindable::class.java.simpleName
                            }"
                        )

                    this.addService(bindable)
                }
            }
            .build()
            .start()

        this.server = server

        DesolveDistcacheService
            .configureShutdownHook()

        val heartbeat = Executors
            .newSingleThreadScheduledExecutor()

        DesolveServiceShutdown.supply {
            stop()
            heartbeat.shutdownNow()
        }

        // Although our service IDs are auto-generated,
        // we can just ignore entries that are considered "dead"
        heartbeat.scheduleAtFixedRate(
            DesolveServiceHeartbeat(this)
                .apply {
                    DesolveServiceShutdown.supply { close() }
                },
            0L, 1L, TimeUnit.SECONDS
        )

        val elapsed =
            System.currentTimeMillis() - millis

        DesolveServiceLogger.logger()
            .info(
                "[Desolve] ${
                    this.provider.serviceId.capitalize()
                } started in ${elapsed}ms (${
                    elapsed / 1000L
                }s)."
            )

        this.blockUntilShutdown()
    }

    private fun stop()
    {
        server?.shutdownNow()
    }

    private fun blockUntilShutdown()
    {
        server?.awaitTermination()
    }
}
