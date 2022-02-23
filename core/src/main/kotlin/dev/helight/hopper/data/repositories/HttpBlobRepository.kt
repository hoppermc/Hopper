package dev.helight.hopper.data.repositories

import dev.helight.hopper.data.PersistentEntity
import dev.helight.hopper.data.SimpleCrudRepository
import dev.helight.hopper.offstageAsync
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.closeQuietly
import okio.BufferedSink
import okio.Source
import okio.source
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

class HttpBlobRepository(
    val baseUrl: String,
    val authorization: String,
    val charset: Charset = Charsets.UTF_8,
    val mediaType: MediaType? = "text/plain".toMediaTypeOrNull()
) : SimpleCrudRepository<HttpBlobRepository.BlobEntity> {

    override suspend fun create(entity: BlobEntity) {
        upload(entity.id, mediaType, entity.content.byteInputStream(charset)).join()
    }

    override suspend fun update(entity: BlobEntity) {
        error("Not supported by this implementation")
    }

    override suspend fun delete(entity: BlobEntity) {
        error("Not supported by this implementation")
    }

    override suspend fun get(id: String): BlobEntity {
        val buffer = ByteArrayOutputStream()
        retrieve(id, buffer).join()
        return BlobEntity(id, buffer.toByteArray().toString(charset))
    }

    override suspend fun list(): List<BlobEntity> {
        error("Not supported by this implementation")
    }

    data class BlobEntity(override var id: String, val content: String) : PersistentEntity()

    fun retrieve(id: String, output: OutputStream) = offstageAsync {
        val client = OkHttpClient()
        val request = Request.Builder()
            .get()
            .url("$baseUrl/$id")
            .header("Authorization", authorization)
            .build()
        val response = client.newCall(request).execute()
        val body = response.body!!
        body.byteStream().copyTo(output)
    }

    fun upload(id: String, mediaType: MediaType?, input: InputStream) = offstageAsync {
        val client = OkHttpClient()

        val request = Request.Builder()
            .put(createBody(mediaType, input))
            .url("$baseUrl/$id")
            .header("Authorization", authorization)
            .build()
        val response = client.newCall(request).execute()
    }

    private fun createBody(mediaType: MediaType?, inputStream: InputStream): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun contentLength(): Long {
                return try {
                    inputStream.available().toLong()
                } catch (e: IOException) {
                    0
                }
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                var source: Source? = null
                try {
                    source = inputStream.source()
                    sink.writeAll(source)
                } finally {
                    source?.closeQuietly()
                }
            }
        }
    }

    companion object {
        fun fromBasicAuth(baseUrl: String, user: String, password: String,
                         charset: Charset = Charsets.UTF_8, mediaType: MediaType? = "text/plain".toMediaTypeOrNull()): HttpBlobRepository =
            HttpBlobRepository(
                baseUrl, "Basic ${Base64.getEncoder().encodeToString("$user:$password".toByteArray(charset))}", charset, mediaType
            )
    }
}