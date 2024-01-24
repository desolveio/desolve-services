package io.desolve.services.library

import com.google.common.base.Suppliers
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

/**
 * @author GrowlyX
 * @since 10/14/2021
 */
object DesolveLibraryLoader
{
    @JvmStatic
    private val LOGGER = Logger.getAnonymousLogger()

    @JvmStatic
    private val CLASS_LOADER = this::class.java.classLoader

    @JvmStatic
    private val URL_INJECTOR = Suppliers.memoize {
        URLClassLoaderAccess.create(
            CLASS_LOADER as URLClassLoader
        )
    }

    private val libraryDirectory = File("libraries")
        .apply {
            this.mkdirs()
        }

    private fun loadLibrary(
        library: DesolveLibrary,
        artifact: String
    )
    {
        val name = "${library.artifactId}-${library.version}${if (library.shadedClassifier) "-shaded" else ""}"

        LOGGER.info(
            "[Library] Loading dependency $name..."
        )

        val saveLocation = File(this.libraryDirectory, "$name.jar")

        if (!saveLocation.exists())
        {
            val time = measureTimeMillis {
                getUrl(artifact, library)
                    .openStream().apply {
                        Files.copy(this, saveLocation.toPath())
                    }
            }

            LOGGER.info("[Library] Downloaded dependency $name in ${time}ms.")
        }

        if (!saveLocation.exists())
        {
            throw IllegalStateException("Unable to download dependency: $library")
        }

        kotlin.runCatching {
            URL_INJECTOR.get().addURL(
                saveLocation.toURI().toURL()
            )
        }.onFailure {
            LOGGER.log(Level.SEVERE, "Failed to inject library into runtime.", it)
        }.onSuccess {
            LOGGER.info("[Library] Loaded library $name successfully.")
        }
    }

    @Throws(MalformedURLException::class)
    private fun getUrl(
        repoUrl: String,
        library: DesolveLibrary
    ): URL
    {
        var repo = repoUrl

        if (!repo.endsWith("/"))
        {
            repo += "/"
        }

        repo += "%s/%s/%s/%s-%s.jar"

        val url = String.format(
            repo,
            library.groupId.replace(".", "/"),
            library.artifactId,
            library.version,
            library.artifactId,
            if (library.shadedClassifier)
                "${library.version}-shaded" else library.version
        )

        return URL(url)
    }
}
