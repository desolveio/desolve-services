package io.desolve.services.core.suspend

import io.desolve.services.core.DesolveServiceLogger
import kotlinx.coroutines.runBlocking
import java.util.logging.Level

/**
 * @author GrowlyX
 * @since 6/10/2022
 */
interface SuspendingRunnable : Runnable
{
    override fun run()
    {
        runBlocking {
            kotlin.runCatching { suspended() }
                .onFailure {
                    DesolveServiceLogger.logger()
                        .log(Level.WARNING, "Exception thrown in coroutine block", it)
                }
        }
    }

    suspend fun suspended()
}
