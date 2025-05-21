package com.prototipo.rehabparkinson

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import android.util.Log
import org.opencv.android.OpenCVLoader

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Enlaza con el XML de login

        // OpenCV debe ser iniciado!
        if (OpenCVLoader.initLocal()) {
            Log.d("LOADED", "OpenCV loaded successfully")
        } else {
            Log.d("LOADED", "OpenCV failed to load")
            return
        }

        val etExpNumber = findViewById<EditText>(R.id.etExpNumber)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val expNumber = etExpNumber.text.toString()

            if (expNumber.isNotEmpty()) {
                // Ir a MainActivity solo si el campo no está vacío
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, ingresa tu número de expediente", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
