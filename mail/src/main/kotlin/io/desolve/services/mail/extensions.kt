package io.desolve.services.mail

import java.io.File
import java.io.InputStream

/**
 * @author GrowlyX
 * @since 6/18/2022
 */
fun resource(resource: String): InputStream
{
    var builtIn = DesolveMailService.javaClass
        .getResourceAsStream(resource)

    if (builtIn == null)
    {
        val file = File(resource)

        builtIn = if (!file.exists())
            null else file.inputStream()
    }

    return builtIn
        ?: throw IllegalArgumentException(
            "Resource by name $resource does not exist."
        )
}
