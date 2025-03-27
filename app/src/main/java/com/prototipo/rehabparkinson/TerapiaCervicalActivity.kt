package com.prototipo.rehabparkinson

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.VideoView
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity

class TerapiaCervicalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terapia_cervical)

        val btnRealizar = findViewById<Button>(R.id.btnRealizar)
        val btnEjemplo = findViewById<Button>(R.id.btnEjemplo)
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        val videoView = findViewById<VideoView>(R.id.videoView)

        // 🔹 🔥 SOLUCIÓN: Asegurar que videoContainer y btnCerrarVideo sean reconocidos
        val videoContainer = findViewById<View>(R.id.videoContainer)
        val btnCerrarVideo = findViewById<Button>(R.id.btnCerrarVideo)

        // 🔹 Ir a IndicacionesActivity al presionar "Realizar"
        btnRealizar.setOnClickListener {
            val intent = Intent(this, IndicacionesActivity::class.java)
            startActivity(intent)
        }

        // 🔹 Mostrar el video al presionar "Ejemplo"
        btnEjemplo.setOnClickListener {
            videoContainer.visibility = View.VISIBLE  // Muestra el contenedor con el video
            val videoUri = Uri.parse("android.resource://$packageName/${R.raw.ejemplo_terapia}")
            videoView.setVideoURI(videoUri)

            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            videoView.start() // Iniciar reproducción
        }

        // 🔹 Cerrar el video al presionar el botón "Cerrar"
        btnCerrarVideo.setOnClickListener {
            videoContainer.visibility = View.GONE // Oculta el video
            videoView.stopPlayback()
        }

        // 🔹 Volver a MainActivity al presionar "Volver"
        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
