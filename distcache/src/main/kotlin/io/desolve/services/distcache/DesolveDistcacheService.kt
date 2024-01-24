package io.desolve.services.distcache

import io.desolve.services.containers.DesolveContainerHelper
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
object DesolveDistcacheService
{
    val containers =
        mutableMapOf<KClass<out DesolveDistcacheContainer>, DesolveDistcacheContainer>()

    private val client =
        RedisClient.create(
            RedisURI.create(DesolveContainerHelper.address(), 6379)
        )

    fun client(): RedisClient = client

    fun configureShutdownHook()
    {
        Runtime.getRuntime()
            .addShutdownHook(Thread {
                client.shutdown()
            })
    }

    inline fun <reified T : DesolveDistcacheContainer> container(): T
    {
        return this.containers[T::class]!! as T
    }

    fun container(
        container: DesolveDistcacheContainer
    )
    {
        this.containers[container::class] = container
    }
}
