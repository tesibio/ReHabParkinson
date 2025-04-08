package com.prototipo.rehabparkinson

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class IndicacionesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indicaciones)

        val btnSiguiente = findViewById<Button>(R.id.btnSiguiente)
        val btnSalir = findViewById<Button>(R.id.btnSalir)

        // 🔹 Ir a TerapiaEjercicioActivity al presionar "Siguiente"
        btnSiguiente.setOnClickListener {
            val intent = Intent(this, ExerciseCameraActivity::class.java)
            startActivity(intent)
        }

        // 🔹 Volver al MainActivity al presionar "Salir"
        btnSalir.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
