package com.prototipo.rehabparkinson

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

import android.util.Log
import org.opencv.android.OpenCVLoader

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
            val usuarios = cargarUsuariosDesdeAssets() // ✅ usamos la función

            val usuario = usuarios.find { it.numExpediente == expNumber }

            if (usuario != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("nombreUsuario", "${usuario.nombre} ${usuario.apellido}")
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Número de expediente no válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

     private fun cargarUsuariosDesdeAssets(): List<Usuario> {
        val inputStream = assets.open("users.json")
        val reader = InputStreamReader(inputStream)
        val tipoLista = object : TypeToken<List<Usuario>>() {}.type
        return Gson().fromJson(reader, tipoLista)
    }
}
