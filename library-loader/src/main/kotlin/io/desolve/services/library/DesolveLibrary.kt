package io.desolve.services.library

/**
 * @author GrowlyX
 * @since 7/31/2022
 */
data class DesolveLibrary(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val repository: String = "https://repo1.maven.org/maven2",
    val shadedClassifier: Boolean = false
)
