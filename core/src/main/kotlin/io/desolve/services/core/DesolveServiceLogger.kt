package io.desolve.services.core

import java.util.logging.Logger

/**
 * @author GrowlyX
 * @since 5/22/2022
 */
object DesolveServiceLogger
{
    private val logger =
        Logger.getGlobal()

    fun logger(): Logger = logger
}
