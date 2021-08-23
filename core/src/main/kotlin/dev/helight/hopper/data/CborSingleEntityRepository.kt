package dev.helight.hopper.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.io.File

@ExperimentalSerializationApi
object CborSingleEntityRepository {

    inline fun <reified T> file(): File {
        val directory = File("cbor-data")
        if (!directory.exists()) directory.mkdir()
        return File(directory, "${T::class.java.simpleName.lowercase()}.cbor")
    }

    inline fun <reified T> read(): T? = when (file<T>().exists()) {
        true -> Cbor.decodeFromByteArray<T>(file<T>().readBytes())
        false -> null
    }

    inline fun <reified T> write(value: T) {
        val file = file<T>()
        if (!file.exists()) file.createNewFile()
        file.writeBytes(Cbor.encodeToByteArray(value))
    }
}