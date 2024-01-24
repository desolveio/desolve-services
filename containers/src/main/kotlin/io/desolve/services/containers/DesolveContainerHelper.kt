package io.desolve.services.containers

/**
 * @author GrowlyX
 * @since 6/9/2022
 */
object DesolveContainerHelper
{
    private val docker = System
        .getenv("DESOLVE_DOCKER")
        ?.toBoolean() ?: false

    private val kubernetes = System
        .getenv("DESOLVE_K8S")
        ?.toBoolean() ?: false

    fun containerized() = docker
    fun kubernetes() = kubernetes

    fun address() = if (docker)
        "172.17.0.1" else "127.0.0.1"

    fun containerId() = if (docker)
        System.getenv("HOSTNAME") else ""
}
