package io.desolve.services.store

import kotlinx.serialization.Serializable

/**
 * @author GrowlyX
 * @since 6/15/2022
 */
interface DesolveDataModel<K>
{
    // TODO: 6/15/2022 ensure stuff works with kmongo without
    //  the stupid _id sometime in the future
    @Serializable
    val _id: K
}
