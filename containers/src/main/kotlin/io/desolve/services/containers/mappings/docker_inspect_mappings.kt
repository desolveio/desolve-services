package io.desolve.services.containers.mappings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * @author GrowlyX
 * @since 6/21/2022
 */
fun String.inspectModel() =
    Json.decodeFromString<List<DockerInspectModel>>(this)

@Serializable
data class DockerInspectModel(
    @SerialName("Id")
    val containerId: String,
    @SerialName("Name")
    val containerName: String,
    @SerialName("State")
    val state: DockerInspectState,
    @SerialName("HostConfig")
    val hostConfig: DockerInspectHostConfig
)

@Serializable
data class DockerInspectState(
    @SerialName("Status")
    val status: String,
    @SerialName("Running")
    val running: Boolean
)

@Serializable
data class DockerInspectHostConfig(
    @SerialName("PortBindings")
    val portBindings: Map<String, List<HostConfigPortBindings>>
)
{
    @Serializable
    data class HostConfigPortBindings(
        @SerialName("HostIp")
        val hostIp: String,
        @SerialName("HostPort")
        val hostPort: String
    )
}
