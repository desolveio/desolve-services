package io.desolve.services.store

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.util.KMongoUtil
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 6/15/2022
 */
abstract class DesolveDataStore<T : DesolveDataModel<K>, K>
{
    val desolveModelCollection by lazy {
        // have to do this instead of
        // getCollection<T> since not reified! :(
        DesolveDataStoreMongo
            .database().database
            .getCollection(
                KMongoUtil.defaultCollectionName(getType()),
                getType().java
            )
            .coroutine
    }

    suspend fun update(model: T) =
        this.desolveModelCollection
            .replaceOne(
                Filters.eq("_id", model._id),
                model, ReplaceOptions().upsert(true)
            )

    suspend fun findByUniqueId(uniqueId: K & Any) =
        this.desolveModelCollection.findOneById(uniqueId)

    abstract fun getType(): KClass<T>
}
