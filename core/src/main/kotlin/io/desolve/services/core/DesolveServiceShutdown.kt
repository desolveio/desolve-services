package io.desolve.services.core

/**
 * @author GrowlyX
 * @since 6/21/2022
 */
object DesolveServiceShutdown
{
    private val lambdas =
        mutableListOf<() -> Unit>()

    fun supply(lambda: () -> Unit) =
        this.lambdas.add(lambda)

    fun configure()
    {
        Runtime.getRuntime().addShutdownHook(Thread {
            DesolveServiceLogger.logger().info(
                "[Shutdown] Processing shutdown request..."
            )

            this.lambdas.forEach {
                runCatching(it).onFailure { exception ->
                    exception.printStackTrace()
                }
            }

            DesolveServiceLogger.logger().info(
                "[Shutdown] Completed shutdown"
            )
        })
    }
}
