package com.prototipo.rehabparkinson

import android.content.Intent
import android.content.SharedPreferences
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

        // Cargar OpenCV
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
            val usuarios = cargarUsuariosDesdeAssets()

            val usuario = usuarios.find { it.numExpediente == expNumber }

            if (usuario != null) {
                val nombreCompleto = "${usuario.nombre} ${usuario.apellido}"

                // ✅ Guardamos el nombre en SharedPreferences
                val prefs = getSharedPreferences("datosUsuario", MODE_PRIVATE)
                prefs.edit().putString("nombreUsuario", nombreCompleto).apply()

                // ✅ Ya no es necesario pasar el nombre por Intent
                val intent = Intent(this, MainActivity::class.java)
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
