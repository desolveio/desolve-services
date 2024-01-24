package io.desolve.services.core

import java.util.*

/**
 * @author GrowlyX
 * @since 6/10/2022
 */
fun <T> LinkedList<T>.popOrNull() =
    if (this.isNotEmpty())
        this.pop() else null

fun String.parseUniqueId() =
    kotlin.runCatching {
        UUID.fromString(this)
    }.getOrNull()
