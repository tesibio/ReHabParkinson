package com.prototipo.rehabparkinson

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Botón Terapia Columna Cervical
        val btnCervical = findViewById<Button>(R.id.btnCervical)
        btnCervical.setOnClickListener {
            val intent = Intent(this, TerapiaCervicalActivity::class.java)
            startActivity(intent)
        }

        // Botón Terapia Columna Lumbar (coincide con el nombre real)
        val btnLumbar = findViewById<Button>(R.id.btnLumbar)
        btnLumbar.setOnClickListener {
            val intent = Intent(this, terapiaLumbarActivity::class.java) // Nombre corregido
            startActivity(intent)
        }
    }
}
