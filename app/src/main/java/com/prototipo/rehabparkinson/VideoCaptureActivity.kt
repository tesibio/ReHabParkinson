package com.prototipo.rehabparkinson

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoCaptureActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnGrabar: Button
    private lateinit var videoCapture: VideoCapture<Recorder>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var btnEnviar: Button
    private var videoUri: Uri? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var recording: Recording? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_capture)

        previewView = findViewById(R.id.previewView)
        btnGrabar = findViewById(R.id.btnGrabar)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions.launch(REQUIRED_PERMISSIONS)
        }

        btnGrabar.setOnClickListener {
            if (recording != null) stopRecording() else startRecording()
        }
        btnEnviar = findViewById(R.id.btnEnviar)
        btnEnviar.setOnClickListener {
            val intent = Intent(this, VideoSelectorActivity::class.java)
            startActivity(intent)
        }
        btnEnviar.visibility = View.GONE


        findViewById<Button>(R.id.btnSwitchCamera).setOnClickListener {
            toggleCamera()
        }

        // ⬅️ Aquí la lógica de cancelar
        findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            finish()
        }
    }

    private fun toggleCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + ".mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ReHabParkinson")
            }
        }

        val mediaStoreOutput = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        var pendingRecording = videoCapture.output.prepareRecording(this, mediaStoreOutput)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            pendingRecording = pendingRecording.withAudioEnabled()
        }

        recording = pendingRecording.start(ContextCompat.getMainExecutor(this)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    Toast.makeText(this, "Grabando…", Toast.LENGTH_SHORT).show()
                    btnGrabar.text = "Detener"
                    iniciarTemporizador(2 * 60 * 1000)
                }
                is VideoRecordEvent.Finalize -> {
                    btnGrabar.text = "Grabar"
                    if (event.error != VideoRecordEvent.Finalize.ERROR_NONE) {
                        Toast.makeText(this, "Error al guardar video", Toast.LENGTH_LONG).show()
                    } else {
                        videoUri = event.outputResults.outputUri
                        btnEnviar.visibility = View.VISIBLE
                        Toast.makeText(this, "Video guardado correctamente", Toast.LENGTH_SHORT).show()
                    }

//                    if (event.error != VideoRecordEvent.Finalize.ERROR_NONE) {
//                        Log.e(TAG, "Error grabación: ${event.error}", event.cause)
//                        Toast.makeText(this, "Error al guardar video", Toast.LENGTH_LONG).show()
//                    } else {
//                        Toast.makeText(
//                            this,
//                            "Video guardado: ${event.outputResults.outputUri}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
                    recording = null
                }
            }
        }
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
    }

    private fun iniciarTemporizador(millis: Long) {
        object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                stopRecording()
            }
        }.start()
    }

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (REQUIRED_PERMISSIONS.all { perms[it] == true }) {
            startCamera()
        } else {
            Toast.makeText(this, "Permisos requeridos no concedidos.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "VideoCaptureActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        }.toTypedArray()
    }
}
