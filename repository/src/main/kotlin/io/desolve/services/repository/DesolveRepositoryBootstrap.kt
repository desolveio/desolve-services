package io.desolve.services.repository

import io.desolve.services.core.DesolveServiceLogger
import io.desolve.services.core.DesolveServiceMeta
import io.desolve.services.core.client.DesolveClientConstants
import io.desolve.services.core.client.DesolveClientService
import io.desolve.services.protocol.StowageGrpcKt
import io.desolve.services.repository.distcache.DesolveRepositoryDistcacheService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.Log4j2Metrics
import io.micrometer.core.instrument.binder.mongodb.DefaultMongoCommandTagsProvider
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

/**
 * @author GrowlyX
 * @since 5/24/2022
 */
object DesolveRepositoryBootstrap
{
    @JvmStatic
    fun main(args: Array<String>)
    {
        val artifactChannel = DesolveClientConstants
            .build(DesolveServiceMeta.Artifacts)

        // TODO: 5/24/2022 close client
        val artifactClient =
            DesolveClientService(
                artifactChannel,
                StowageGrpcKt
                    .StowageCoroutineStub(
                        artifactChannel
                    )
            )

        DesolveRepositoryDistcacheService.configure()

        Runtime.getRuntime().addShutdownHook(Thread {
            DesolveServiceLogger.logger().info(
                "[Shutdown] Shutting down artifact client"
            )

            artifactClient.close()
        })

        embeddedServer(
            Netty, port = 8080
        ) {
            val registry =
                PrometheusMeterRegistry(
                    PrometheusConfig.DEFAULT
                )

            install(IgnoreTrailingSlash)
            install(MicrometerMetrics) {
                this.registry = registry
                this.meterBinders = listOf(
                    ClassLoaderMetrics(),
                    JvmMemoryMetrics(),
                    JvmGcMetrics(),
                    ProcessorMetrics(),
                    JvmThreadMetrics(),
                    JvmInfoMetrics(),
                    FileDescriptorMetrics(),
                    UptimeMetrics()
                )
            }

            routing {
                DesolveRepositoryRouting(artifactClient)
                    .route(this)

                get("/internal/metrics") {
                    // TODO: "firewall" - call.request.local.remoteHost
                    call.respondText(registry.scrape())
                }
            }
        }.start(
            wait = true
        )
    }
}
