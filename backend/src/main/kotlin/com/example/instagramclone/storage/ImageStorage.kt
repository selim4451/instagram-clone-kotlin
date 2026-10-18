package com.example.instagramclone.storage

import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyAndClose
import java.io.File
import java.util.UUID

class ImageStorage(val directory: File) {

    init {
        directory.mkdirs()
    }

    suspend fun save(content: ByteReadChannel, extension: String): String {
        val name = "${UUID.randomUUID()}.$extension"
        content.copyAndClose(File(directory, name).writeChannel())
        return name
    }

    fun delete(name: String) {
        File(directory, name).delete()
    }
}
