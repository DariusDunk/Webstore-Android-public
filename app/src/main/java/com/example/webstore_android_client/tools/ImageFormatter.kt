package com.example.webstore_android_client.tools

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

fun prepareImagePart(context: Context, uri: Uri, partName: String = "image"): MultipartBody.Part? {
    val contentResolver = context.contentResolver
    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        val tempFile = File.createTempFile("upload_image", ".jpg", context.cacheDir)

        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
        MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}