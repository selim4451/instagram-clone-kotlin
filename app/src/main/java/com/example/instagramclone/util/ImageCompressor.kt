package com.example.instagramclone.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object ImageCompressor {

    private const val MAX_SIZE = 1080
    private const val JPEG_QUALITY = 85

    fun compress(resolver: ContentResolver, uri: Uri): ByteArray {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                val scale = MAX_SIZE.toFloat() / max(info.size.width, info.size.height)
                if (scale < 1f) {
                    decoder.setTargetSize(
                        (info.size.width * scale).roundToInt(),
                        (info.size.height * scale).roundToInt(),
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            decodeLegacy(resolver, uri)
        }

        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            output.toByteArray()
        }
    }

    private fun decodeLegacy(resolver: ContentResolver, uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }

        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_SIZE) sampleSize *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Image could not be decoded")
    }
}
