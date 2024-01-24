package io.desolve.services.distcache

import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.serialization.json.Json

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
abstract class DesolveDistcacheContainer
{
    private val connection by lazy {
        DesolveDistcacheService.client().connect()
    }

    fun connection(): StatefulRedisConnection<String, String> = connection
    fun serializer() = Json
}
