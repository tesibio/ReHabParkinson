package com.prototipo.rehabparkinson

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtener el nombre del usuario desde SharedPreferences
        val prefs = getSharedPreferences("datosUsuario", MODE_PRIVATE)
        val nombreUsuario = prefs.getString("nombreUsuario", "usuario")

        val tvUsuario = findViewById<TextView>(R.id.tvUsuario)
        tvUsuario.text = getString(R.string.bienvenida_con_nombre, nombreUsuario)

        // Botón Terapia Columna Cervical
        val btnCervical = findViewById<Button>(R.id.btnCervical)
        btnCervical.setOnClickListener {
            val intent = Intent(this, TerapiaCervicalActivity::class.java)
            startActivity(intent)
        }

        // Botón Terapia Columna Lumbar
        val btnLumbar = findViewById<Button>(R.id.btnLumbar)
        btnLumbar.setOnClickListener {
            val intent = Intent(this, terapiaLumbarActivity::class.java)
            startActivity(intent)
        }
    }
}