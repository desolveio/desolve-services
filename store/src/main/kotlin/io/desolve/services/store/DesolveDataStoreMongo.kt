package io.desolve.services.store

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import io.desolve.services.containers.DesolveContainerHelper
import okhttp3.internal.closeQuietly
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

/**
 * @author GrowlyX
 * @since 6/15/2022
 */
object DesolveDataStoreMongo
{
    private val connectionString: String? = System
        .getenv("DESOLVE_MONGO_CONNECTION_STRING")

    private val settings = MongoClientSettings.builder()
        .uuidRepresentation(UuidRepresentation.STANDARD)
        .applyConnectionString(
            ConnectionString(
                connectionString
                    ?: "mongodb://${DesolveContainerHelper.address()}:27017/"
            )
        )
        .build()

    private val client = KMongo
        .createClient(settings)
        .coroutine

    private val desolveDatabase = this
        .client.getDatabase("Desolve")

    fun database() = desolveDatabase
    fun close() = client.closeQuietly()

    // prevent multiple shutdown hooks
    private var configured = false

    fun configureJvmShutdown()
    {
        if (configured)
            return

        Runtime.getRuntime()
            .addShutdownHook(
                Thread(this::close)
            )
    }
}
