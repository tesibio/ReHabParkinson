package com.prototipo.rehabparkinson

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.VideoView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.io.File

import com.google.api.services.drive.Drive


class VideoSelectorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoPreview: VideoView
    private lateinit var previewContainer: FrameLayout
    private lateinit var btnCerrar: Button
    private lateinit var btnSubir: Button

    private lateinit var adapter: VideoAdapter
    private var selectedVideoFile: File? = null

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

        val videos = getVideosFromFolder()
        adapter.updateList(videos)

        btnCerrar.setOnClickListener {
            videoPreview.stopPlayback()
            previewContainer.visibility = View.GONE
        }

        btnSubir.setOnClickListener {
            val file = selectedVideoFile
            if (file != null) {
                DriveUploader.iniciarSesion(this) { driveService: Drive ->
                    DriveUploader.uploadFile(this, file) { exito ->
                        if (exito) {
                            Toast.makeText(this, "Subida completada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error en la subida", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
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
                DriveUploader.manejarResultadoSignIn(account, this) { drive ->
                    // Reintenta la subida si aún hay un archivo seleccionado
                    selectedVideoFile?.let { file ->
                        DriveUploader.uploadFile(this, file) { exito ->
                            Toast.makeText(this, if (exito) "Subido" else "Falló la subida", Toast.LENGTH_SHORT).show()
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


