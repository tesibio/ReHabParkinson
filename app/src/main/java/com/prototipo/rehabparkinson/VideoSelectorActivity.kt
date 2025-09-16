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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

private const val TAG = "VideoSelectorActivity"

class VideoSelectorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoPreview: VideoView
    private lateinit var previewContainer: FrameLayout
    private lateinit var btnCerrar: FloatingActionButton
    private lateinit var btnSubir: Button

    private lateinit var adapter: VideoAdapter
    private var selectedVideoFile: File? = null
    private var archivoFinal: File? = null  // archivo a subir

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

        btnSubir.setOnClickListener {
            val file = selectedVideoFile
            if (file != null) {
                Log.d(TAG, "Archivo seleccionado: ${file.absolutePath}")
                Log.d(TAG, "¿Existe el archivo? ${file.exists()}")

                archivoFinal = file

                if (!archivoFinal!!.exists()) {
                    Log.e(TAG, "ERROR: El archivo no existe al intentar subir.")
                    Toast.makeText(this, "Error: el archivo no existe para subir", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // 🔹 Subida Multipart con UploadHelper
                val serverUrl = "https://tesibio.com/upload.php"
                //val serverUrl = "https://server-6673567914.us-central1.run.app/upload"
                //val serverUrl = "https://webhook.site/0f85fc2f-ee75-4894-a7b4-9ac60f496808"

                //val serverUrl = "https://httpbin.org/post"  //val serverUrl = "http://<hostname>/upload" // ⚠️ cámbialo por tu servidor real
                UploadHelper.uploadFile(this, Uri.fromFile(archivoFinal!!), serverUrl, object : Callback {
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        runOnUiThread {
                            Log.e(TAG, "Error al subir: ${e.message}", e)
                            Toast.makeText(applicationContext, "Error al subir: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {
                            if (response.isSuccessful) {
                                Log.d(TAG, "Subida exitosa. Respuesta: ${response.body?.string()}")
                                Toast.makeText(applicationContext, "Subida exitosa", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e(TAG, "Error en servidor: ${response.code}")
                                Toast.makeText(applicationContext, "Error en servidor: ${response.code}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                })

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

    private fun getVideosFromFolder(): List<File> {
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .resolve("ReHabParkinson")
        return folder.listFiles { _, name ->
            name.endsWith(".mp4")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
