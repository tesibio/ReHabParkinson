package com.prototipo.rehabparkinson

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.drive.Drive
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "VideoSelectorActivity"

class VideoSelectorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoPreview: VideoView
    private lateinit var previewContainer: FrameLayout
    private lateinit var btnCerrar: Button
    private lateinit var btnSubir: Button

    private lateinit var adapter: VideoAdapter
    private var selectedVideoFile: File? = null
    private var archivoFinal: File? = null  // ⬅️ Global para reintento post login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_selector)

        recyclerView = findViewById(R.id.recyclerVideos)
        videoPreview = findViewById(R.id.videoPreview)
        previewContainer = findViewById(R.id.videoPreviewContainer)
        btnCerrar = findViewById(R.id.btnCerrarPreview)
        btnSubir = findViewById(R.id.btnSubirSeleccionado)

        adapter = VideoAdapter { file ->
            selectedVideoFile = file
            btnSubir.isEnabled = true
            previewContainer.visibility = View.VISIBLE
            videoPreview.setVideoURI(Uri.fromFile(file))
            videoPreview.start()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.updateList(getVideosFromFolder())

        btnCerrar.setOnClickListener {
            videoPreview.stopPlayback()
            previewContainer.visibility = View.GONE
        }

        val tipoEjercicio = intent.getStringExtra("tipoEjercicio") ?: "CE"
        val prefs = getSharedPreferences("datosUsuario", MODE_PRIVATE)
        val expediente = prefs.getString("numExpediente", "000") ?: "000"

        btnSubir.setOnClickListener {
            val file = selectedVideoFile
            if (file != null) {
                Log.d(TAG, "Archivo seleccionado: ${file.absolutePath}")
                Log.d(TAG, "¿Existe antes de renombrar? ${file.exists()}")

                val fecha = SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date(file.lastModified()))
                val nuevoNombre = "${tipoEjercicio}${fecha}_${expediente}.mp4"
                Log.d(TAG, "Nuevo nombre esperado: $nuevoNombre")

                val renamedFile = File(file.parentFile, nuevoNombre)
                val renameSuccess = file.renameTo(renamedFile)
                Log.d(TAG, "¿Renombrado con éxito? $renameSuccess")
                Log.d(TAG, "Ruta final: ${renamedFile.absolutePath}")
                Log.d(TAG, "¿Existe el archivo final? ${renamedFile.exists()}")

                archivoFinal = if (renameSuccess) renamedFile else file

                if (!archivoFinal!!.exists()) {
                    Log.e(TAG, "ERROR: El archivo no existe al intentar subir.")
                    Toast.makeText(this, "Error: el archivo no existe para subir", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                DriveUploader.iniciarSesion(this) {
                    Log.d(TAG, "Sesión Google iniciada. Subiendo archivo: ${archivoFinal!!.name}")
                    DriveUploader.uploadFile(this, archivoFinal!!) { exito ->
                        Log.d(TAG, "Resultado de la subida: $exito")
                        Toast.makeText(this, if (exito) "Subida completada" else "Error en la subida", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.w(TAG, "No hay archivo seleccionado para subir.")
                Toast.makeText(this, "No has seleccionado un archivo", Toast.LENGTH_SHORT).show()
            }
        }

        val btnVolver = findViewById<Button>(R.id.btnVolver)
        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DriveUploader.REQUEST_CODE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.result
                DriveUploader.manejarResultadoSignIn(account, this) {
                    archivoFinal?.let { archivo ->
                        Log.d(TAG, "Reintentando subida tras login. Archivo: ${archivo.absolutePath}")
                        if (archivo.exists()) {
                            DriveUploader.uploadFile(this, archivo) { exito ->
                                Toast.makeText(this, if (exito) "Subido" else "Falló la subida", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.e(TAG, "Archivo no encontrado tras login: ${archivo.absolutePath}")
                            Toast.makeText(this, "Archivo no encontrado tras autenticación", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error de autenticación con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getVideosFromFolder(): List<File> {
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .resolve("ReHabParkinson")
        return folder.listFiles { _, name ->
            name.endsWith(".mp4")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
