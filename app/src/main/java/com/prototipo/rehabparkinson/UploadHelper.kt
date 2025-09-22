package com.prototipo.rehabparkinson

import android.app.Activity   // ✅ necesario para el cast
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.FileOutputStream

object UploadHelper {
    private val client = OkHttpClient()

    fun uploadFile(
        context: Context,
        uri: Uri,
        serverUrl: String,
        onProgress: (Int) -> Unit,
        callback: Callback
    ) {
        // ✅ usar el nombre real si el Uri es "file://"
        val file = if (uri.scheme == "file") {
            File(uri.path!!)
        } else {
            uriToFile(context, uri)
        }

        val requestBody = ProgressRequestBody(file, "video/mp4") { progress ->
            (context as Activity).runOnUiThread {
                onProgress(progress)  // 🔹 actualiza progreso en la Activity
            }
        }

        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, requestBody)
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .post(multipart)
            .build()

        client.newCall(request).enqueue(callback)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        var name: String? = null

        // Intentar recuperar el nombre real desde MediaStore
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
        if (nameIndex >= 0 && returnCursor != null && returnCursor.moveToFirst()) {
            name = returnCursor.getString(nameIndex)
        }
        returnCursor?.close()

        // Si no se pudo obtener, generamos uno único
        val finalName = name ?: "video_${System.currentTimeMillis()}.mp4"

        // Copiar el contenido del Uri al caché con ese nombre
        val file = File(context.cacheDir, finalName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}
