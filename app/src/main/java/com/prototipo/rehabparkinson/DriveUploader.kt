package com.prototipo.rehabparkinson

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.Locale

object DriveUploader {

    private const val TAG = "DriveUploader"
    private const val ROOT_FOLDER_ID = "1PrxzVbT6mZSvKBP8DTbH-bxTiBclKPUN" // carpeta fija
    private var driveService: Drive? = null

    const val REQUEST_CODE_SIGN_IN = 9001

    private val REQUIRED_SCOPES = listOf(
        com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE)
    )

    fun iniciarSesion(activity: Activity, onReady: (Drive) -> Unit) {
        val signInAccount = GoogleSignIn.getLastSignedInAccount(activity)

        if (signInAccount != null && signInAccount.grantedScopes.containsAll(REQUIRED_SCOPES)) {
            inicializarServicio(activity, signInAccount, onReady)
        } else {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE))
                .build()

            val client = GoogleSignIn.getClient(activity, gso)
            val signInIntent = client.signInIntent
            activity.startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
        }
    }

    fun manejarResultadoSignIn(account: GoogleSignInAccount, context: Context, onReady: (Drive) -> Unit) {
        inicializarServicio(context, account, onReady)
    }

    private fun inicializarServicio(context: Context, account: GoogleSignInAccount, onReady: (Drive) -> Unit) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("ReHabParkinson").build()

        onReady(driveService!!)
    }

    fun uploadFile(context: Context, file: java.io.File, onComplete: (Boolean) -> Unit) {
        val service = driveService
        if (service == null) {
            Toast.makeText(context, "Google Drive no está inicializado", Toast.LENGTH_SHORT).show()
            onComplete(false)
            return
        }

        Thread {
            try {
                // Obtener nombre de carpeta del usuario actual
                val subfolderName = obtenerNombrePacienteDesdePrefs(context)
                if (subfolderName == null) {
                    Log.e(TAG, "No se pudo obtener el nombre del paciente")
                    (context as Activity).runOnUiThread {
                        Toast.makeText(context, "Error: paciente no identificado", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                    return@Thread
                }

                val subfolderId = obtenerOCrearSubcarpeta(service, subfolderName)

                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = file.name
                    parents = listOf(subfolderId)
                }

                val mediaContent = InputStreamContent("video/mp4", file.inputStream())

                val uploadedFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()

                Log.d(TAG, "Archivo subido: ${uploadedFile.id}")
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "Video subido a Drive", Toast.LENGTH_SHORT).show()
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al subir a Drive", e)
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "Error al subir video", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
        }.start()
    }

    private fun obtenerNombrePacienteDesdePrefs(context: Context): String? {
        val prefs = context.getSharedPreferences("datosUsuario", Context.MODE_PRIVATE)
        val expediente = prefs.getString("numExpediente", null) ?: return null

        val inputStream = context.assets.open("users.json")
        val reader = InputStreamReader(inputStream)
        val tipoLista = object : TypeToken<List<Usuario>>() {}.type
        val listaUsuarios: List<Usuario> = Gson().fromJson(reader, tipoLista)

        val usuario = listaUsuarios.find { it.numExpediente == expediente }
        return usuario?.let {
            "${it.apellido}_${it.nombre}".replace(" ", "")
        }
    }

    private fun obtenerOCrearSubcarpeta(service: Drive, subfolderName: String): String {
        // Verifica si ya existe la subcarpeta dentro de la carpeta raíz
        val query = "mimeType = 'application/vnd.google-apps.folder' and name = '$subfolderName' and '${ROOT_FOLDER_ID}' in parents and trashed = false"
        val result = service.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val folder = result.files.firstOrNull()
        if (folder != null) return folder.id

        // Si no existe, la crea
        val metadata = com.google.api.services.drive.model.File().apply {
            name = subfolderName
            mimeType = "application/vnd.google-apps.folder"
            parents = listOf(ROOT_FOLDER_ID)
        }

        val created = service.files().create(metadata)
            .setFields("id")
            .execute()

        return created.id
    }
}
