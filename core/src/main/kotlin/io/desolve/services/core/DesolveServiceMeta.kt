package io.desolve.services.core

/**
 * @author GrowlyX
 * @since 5/22/2022
 */
enum class DesolveServiceMeta(
    val port: Int, val consul: String
) : DesolveServiceProvider
{
    Workers(50500, "worker")
    {
        override val serviceId = "worker"
    },

    Artifacts(50550, "artifacts")
    {
        override val serviceId = "artifacts"
    };

    companion object
    {
        @JvmStatic
        val SERVICEABLE = listOf(Workers, Artifacts)
    }
}
