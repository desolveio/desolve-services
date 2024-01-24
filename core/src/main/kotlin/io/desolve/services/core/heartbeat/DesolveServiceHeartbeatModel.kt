package io.desolve.services.core.heartbeat

/**
 * @author GrowlyX
 * @since 5/26/2022
 */
@Deprecated("Use Consul")
data class DesolveServiceHeartbeatModel(
    val timestamp: Long, val port: Int
)
