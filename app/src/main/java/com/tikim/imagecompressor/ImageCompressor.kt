package com.tikim.imagecompressor

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class ImageCompressor(
    private val context: Context
) {

    suspend fun compressImage(contentUri: Uri, compressionThreshold: Long): Bitmap? {
        return withContext(Dispatchers.IO) {
            val inputBytes = context
                .contentResolver
                .openInputStream(contentUri)
                ?.use { inputStream ->
                    inputStream.readBytes()
                } ?: return@withContext null

            ensureActive()

            withContext(Dispatchers.Default) {
                val bitmap = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size)
                println("Original size: ${inputBytes.size}")
                ensureActive()

                val mimeType = context.contentResolver.getType(contentUri)
                val compressFormat = when (mimeType) {
                    "image/jpeg" -> Bitmap.CompressFormat.JPEG
                    "image/png" -> Bitmap.CompressFormat.PNG
                    "image/webp" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSLESS
                    } else {
                        Bitmap.CompressFormat.WEBP
                    }
                    else -> Bitmap.CompressFormat.JPEG
                }

                var quality = 90
                var outputBytes: ByteArray

                do {
                    ByteArrayOutputStream().use { outputStream ->
                        bitmap.compress(
                            compressFormat,
                            quality,
                            outputStream
                        )
                        outputBytes = outputStream.toByteArray()
                        quality -= (quality * 0.1f).roundToInt()


                        println("Compressed size: ${outputBytes.size}, $quality")
                    }

                } while (
                    isActive &&
                    compressFormat != Bitmap.CompressFormat.PNG &&
                    outputBytes.size > compressionThreshold &&
                    quality > 5
                )

                ensureActive()
                BitmapFactory.decodeByteArray(outputBytes, 0, outputBytes.size)
            }
        }

    }
}