@file:UseSerializers(DesolveInstantSerializer::class)
package io.desolve.services.artifacts.dao

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

/**
 * @author GrowlyX
 * @since 6/21/2022
 */
@Serializable
data class DesolveStoredArtifactMetrics(
    var firstDownloaded: Instant? = null,
    var lastDownloaded: Instant? = null,
    val downloads: MutableList<Instant> = mutableListOf()
)

object DesolveInstantSerializer : KSerializer<Instant>
{
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DesolveInstantSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant =
        Instant.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Instant)
    {
        encoder.encodeString(value.toString())
    }
}
