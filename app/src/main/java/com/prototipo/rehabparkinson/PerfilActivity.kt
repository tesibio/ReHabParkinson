package com.prototipo.rehabparkinson

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val prefs = getSharedPreferences("datosUsuario", MODE_PRIVATE)
        val nombre = prefs.getString("nombreUsuario", "Usuario")
        val expediente = prefs.getString("numExpediente", "")

        val tvNombre = findViewById<TextView>(R.id.tvPerfilNombre)
        val tvRol = findViewById<TextView>(R.id.tvPerfilRol)
        val tvID = findViewById<TextView>(R.id.tvPerfilID)
        val imgPerfil = findViewById<ImageView>(R.id.imageView)

        // Mostrar nombre y expediente desde SharedPreferences
        tvNombre.text = nombre
        tvRol.text = "Paciente"
        tvID.text = "ID: $expediente"

        // Cargar datos del usuario desde el JSON
        val usuarios = cargarUsuariosDesdeAssets()
        val usuario = buscarUsuarioPorExpediente(usuarios, expediente ?: "")

        usuario?.let {
            val nombreImagen = it.getString("foto").removeSuffix(".jpg") // Ej: perfil_001
            val resId = resources.getIdentifier(nombreImagen, "drawable", packageName)

            if (resId != 0) {
                imgPerfil.setImageResource(resId)
            } else {
                imgPerfil.setImageResource(R.drawable.imagendummy) // Imagen por defecto
            }
        }
    }

    private fun cargarUsuariosDesdeAssets(): JSONArray {
        val inputStream: InputStream = assets.open("users.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        return JSONArray(jsonString)
    }

    private fun buscarUsuarioPorExpediente(jsonArray: JSONArray, expediente: String): JSONObject? {
        for (i in 0 until jsonArray.length()) {
            val usuario = jsonArray.getJSONObject(i)
            if (usuario.getString("numExpediente") == expediente) {
                return usuario
            }
        }
        return null
    }
}
