package io.desolve.services.artifacts.assist

import com.google.protobuf.ByteString
import net.jpountz.lz4.LZ4Factory
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream

/**
 * @author GrowlyX
 * @since 5/23/2022
 */
object DesolveArtifactCompression
{
    @JvmStatic
    private val LZ4 = LZ4Factory.fastestInstance()

    @JvmStatic
    private val LZ4_EXP_ENABLED = System
        .getProperty("io.desolve.services.artifacts.assist.ExperimentalLZ4") != null

    @JvmStatic
    fun decompress(
        content: Map<String, ByteString>
    ): CompletableFuture<Map<String, ByteArray>>
    {
        return CompletableFuture
            .supplyAsync {
                content
                    .mapValues {
                        it.value.toByteArray()
                    }
                    .mapValues {
                        inflateByteArray(it.value)
                    }
            }
    }

    @JvmStatic
    fun compress(directory: File) =
        CompletableFuture
            .supplyAsync {
                mutableMapOf<String, ByteArray>()
                    .apply {
                        directory.listFiles()?.forEach {
                            if (it.isDirectory)
                                return@forEach

                            this[it.name] = compressFile(it)
                        }
                    }
            }!!

    @JvmStatic
    fun inflateByteArray(
        array: ByteArray
    ): ByteArray
    {
        if (!LZ4_EXP_ENABLED)
        {
            val output =
                ByteArrayOutputStream()

            val inflater =
                InflaterOutputStream(output)

            inflater.write(array)
            inflater.flush()
            inflater.close()

            return output
                .toByteArray()
        }

        val decompressedLength = array.size

        val decompressor = LZ4.fastDecompressor()
        val restored = ByteArray(decompressedLength)

        decompressor.decompress(
            array, 0, restored, 0, decompressedLength
        )

        return restored
    }

    @JvmStatic
    fun compressFile(
        file: File
    ): ByteArray
    {
        if (!LZ4_EXP_ENABLED)
        {
            val byteArray = file
                .inputStream().readBytes()

            val outputStream =
                ByteArrayOutputStream()

            val deflate =
                DeflaterOutputStream(outputStream)

            deflate.write(byteArray)
            deflate.flush()
            deflate.close()

            return outputStream
                .toByteArray()
        }

        val byteArray = file.inputStream().readBytes()
        val size = byteArray.size

        val compressor = LZ4.fastCompressor()

        val maxCompressedLength = compressor
            .maxCompressedLength(size)

        val compressed = ByteArray(maxCompressedLength)

        compressor.compress(
            byteArray, 0, size, compressed, 0, maxCompressedLength
        )

        return compressed
    }
}
