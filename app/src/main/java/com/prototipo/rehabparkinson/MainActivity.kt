package com.prototipo.rehabparkinson

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Obtener nombre desde SharedPreferences
        val prefs = getSharedPreferences("datosUsuario", MODE_PRIVATE)
        val nombreUsuario = prefs.getString("nombreUsuario", "usuario")
        val tvUsuario = findViewById<TextView>(R.id.tvUsuario)
        tvUsuario.text = getString(R.string.bienvenida_con_nombre, nombreUsuario)

        // ✅ Botón menú
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            mostrarMenu()
        }

        // ✅ Botón terapia cervical
        val btnCervical = findViewById<Button>(R.id.btnCervical)
        btnCervical.setOnClickListener {
            startActivity(Intent(this, TerapiaCervicalActivity::class.java))
        }

        // ✅ Botón terapia lumbar
        val btnLumbar = findViewById<Button>(R.id.btnLumbar)
        btnLumbar.setOnClickListener {
            startActivity(Intent(this, terapiaLumbarActivity::class.java))
        }
    }

    private fun mostrarMenu() {
        val popup = PopupMenu(this, findViewById(R.id.btnMenu))
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_usuario, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                R.id.menu_version -> {
                    val version = try {
                        packageManager.getPackageInfo(packageName, 0).versionName
                    } catch (e: PackageManager.NameNotFoundException) {
                        "desconocida"
                    }
                    val desarrolladores = """ 
                        Versión de la app: $version 
                        Desarrollado por:
                            - Dr. en C. Aguilar-Pérez, Luis Antonio
                            - Ing. Gotchev Chtereva, Cristian Borislavjov
                            - Dr. en C. Jímenez-Ángeles, Luis
                            - Dra. Salomé Morales Sánchez
                            - Dr. en C. Rojas-Arce Jorge L.""".trimIndent()

                    val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Información de la app")
                        .setMessage(desarrolladores)
                        .setPositiveButton("OK", null)
                        .create()
                    dialog.show()
                    true
                }
                R.id.menu_logout -> {
                    getSharedPreferences("datosUsuario", MODE_PRIVATE).edit().clear().apply()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
}
