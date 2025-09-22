package com.prototipo.rehabparkinson

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
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
import java.io.IOException

private const val TAG = "VideoSelectorActivity"

class VideoSelectorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoPreview: VideoView
    private lateinit var previewContainer: FrameLayout
    private lateinit var btnCerrar: FloatingActionButton
    private lateinit var btnSubir: Button

    private lateinit var adapter: VideoAdapter
    private lateinit var progressBar1: ProgressBar
    private lateinit var progressBar2: ProgressBar

    private var selectedVideoFile: File? = null
    private var archivoFinal: File? = null  // archivo a subir

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_selector)

        progressBar1 = findViewById(R.id.progressBarUpload1)
        progressBar2 = findViewById(R.id.progressBarUpload2)
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

        // 🔹 direcciones de subida Multipart con UploadHelper referencias
        //val serverUrl1 = "https://tesibio.com/upload.php" //Direccion de respaldo de video
        //val serverUrl2 = "https://server-6673567914.us-central1.run.app/upload" //Direccion de servidor cristian
        //val serverUrl = "https://webhook.site/0f85fc2f-ee75-4894-a7b4-9ac60f496808" //Direccion de prueba, cambia con cada apertura de página

        //val serverUrl = "https://httpbin.org/post"  // ⚠️ cámbialo por tu servidor real!!!
        btnSubir.setOnClickListener {
            val file = selectedVideoFile
            if (file != null) {
                if (!file.exists()) {
                    Toast.makeText(this, "Error: el archivo no existe", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val uriFile = Uri.fromFile(file)

                // --- Subida a serverUrl2 ---
                progressBar2.visibility = View.VISIBLE
                progressBar2.progress = 0
                val serverUrl2 = "https://server-6673567914.us-central1.run.app/upload"
                UploadHelper.uploadFile(
                    this,
                    uriFile,
                    serverUrl2,
                    onProgress = { progress ->
                        progressBar2.progress = progress
                    },
                    callback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                progressBar2.visibility = View.GONE
                                Toast.makeText(applicationContext, "Error server 2: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            runOnUiThread {
                                progressBar2.visibility = View.GONE
                                if (response.isSuccessful) {
                                    Toast.makeText(applicationContext, "Subida exitosa a server 2", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(applicationContext, "Error server 2: ${response.code}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )

                // --- Subida a serverUrl1 ---
                progressBar1.visibility = View.VISIBLE
                progressBar1.progress = 0
                val serverUrl1 = "https://tesibio.com/upload.php"
                UploadHelper.uploadFile(
                    this,
                    uriFile,
                    serverUrl1,
                    onProgress = { progress ->
                        progressBar1.progress = progress
                    },
                    callback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                progressBar1.visibility = View.GONE
                                Toast.makeText(applicationContext, "Error server 1: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            runOnUiThread {
                                progressBar1.visibility = View.GONE
                                if (response.isSuccessful) {
                                    Toast.makeText(applicationContext, "Subida exitosa a server 1", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(applicationContext, "Error server 1: ${response.code}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )

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

    private fun getVideosFromFolder(): List<File> {
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .resolve("ReHabParkinson")
        return folder.listFiles { _, name ->
            name.endsWith(".mp4")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
