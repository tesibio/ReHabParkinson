package com.prototipo.rehabparkinson

import android.content.Intent
import android.graphics.text.LineBreaker
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class terapia_ec03() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terapia_ec03)

        val btnRealizar = findViewById<Button>(R.id.btnRealizar)
        val btnEjemplo  = findViewById<Button>(R.id.btnEjemplo)
        val btnVolver   = findViewById<Button>(R.id.btnVolver)
        val videoView   = findViewById<VideoView>(R.id.videoView)

        val videoContainer = findViewById<View>(R.id.videoContainer)
        val btnCerrarVideo = findViewById<Button>(R.id.btnCerrarVideo)

        // Estado inicial
        videoContainer.visibility = View.GONE
        btnVolver.visibility = View.VISIBLE

        // ----- BulletSpan + alineación izquierda + justificado -----
        val tvDescripcion = findViewById<TextView>(R.id.tvDescripcion)

        // Alinear a la izquierda
        tvDescripcion.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        tvDescripcion.gravity = Gravity.START

        // Justificar (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvDescripcion.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        }

        // Lista de pasos
        val pasos = listOf(
            "Párese con los pies separados a la anchura de los hombros y deje los brazos a los costados.",
            "Lleve la mano derecha para tocar el oído izquierdo, sin inclinar el cuello.",
            "Regrese al centro.",
            "Ahora lleve la mano izquierda para tocar el oído derecho, sin inclinar el cuello.",
            "Vuelva al centro.",
            "Hágalo a un ritmo cómodo.",
            "Realice 15 repeticiones por cada lado."
        )

        // Tamaños en dp para la viñeta
        val gapPx = dpToPx(16)          // separación texto-viñeta
        val radiusPx = dpToPx(6)        // 🔴 RADIO MÁS GRANDE de la viñeta (~12dp diámetro)

        val builder = SpannableStringBuilder()
        for (paso in pasos) {
            val start = builder.length
            builder.append(paso).append('\n')
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // API 28+: permite color y radio personalizados
                builder.setSpan(
                    BulletSpan(gapPx, tvDescripcion.currentTextColor, radiusPx),
                    start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                // Compatibilidad: no permite radio, pero mantenemos el gap y alineación
                builder.setSpan(
                    BulletSpan(gapPx),
                    start, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        tvDescripcion.text = builder
        // ----------------------------------------------------------

        // Ir a IndicacionesActivity
        btnRealizar.setOnClickListener {
            val intent = Intent(this, IndicacionesActivity::class.java)
            intent.putExtra("tipoEjercicio", "EC05")
            startActivity(intent)
        }

        // Mostrar video de ejemplo
        btnEjemplo.setOnClickListener {
            videoContainer.visibility = View.VISIBLE
            btnVolver.visibility = View.GONE
            btnRealizar.visibility = View.GONE
            btnEjemplo.visibility = View.GONE


            val videoUri = Uri.parse("android.resource://$packageName/${R.raw.video_ec03}")
            videoView.setVideoURI(videoUri)

            val mediaController = MediaController(this).apply {
                setAnchorView(videoView)
            }
            videoView.setMediaController(mediaController)
            videoView.start()
        }

        // Cerrar el video
        btnCerrarVideo.setOnClickListener {
            videoView.stopPlayback()
            videoContainer.visibility = View.GONE
            btnVolver.visibility = View.VISIBLE
            btnRealizar.visibility = View.VISIBLE
            btnEjemplo.visibility = View.VISIBLE
        }

        // Botón Volver
        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val videoContainer = findViewById<View>(R.id.videoContainer)
        val btnCerrarVideo = findViewById<Button>(R.id.btnCerrarVideo)
        if (videoContainer.visibility == View.VISIBLE) {
            btnCerrarVideo.performClick()
        } else {
            super.onBackPressed()
        }
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
}
