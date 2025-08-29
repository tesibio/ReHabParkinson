package com.prototipo.rehabparkinson

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.services.drive.DriveScopes

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
        findViewById<Button>(R.id.btnCervical).setOnClickListener {
            startActivity(Intent(this, TerapiaCervicalActivity::class.java))
        }

        // ✅ Botón terapia lumbar
        findViewById<Button>(R.id.btnLumbar).setOnClickListener {
            startActivity(Intent(this, terapiaLumbarActivity::class.java))
        }

        // ✅ Botón terapia CL01
        findViewById<Button>(R.id.btnterapia_cl01).setOnClickListener {
            startActivity(Intent(this, terapia_cl01::class.java))
        }

        // ✅ Botón terapia EC03
        findViewById<Button>(R.id.btnterapia_ec03).setOnClickListener {
            startActivity(Intent(this, terapia_ec03::class.java))
        }

        // ✅ Botón terapia EC05
        findViewById<Button>(R.id.btnterapia_ec05).setOnClickListener {
            startActivity(Intent(this, terapia_ec05::class.java))
        }
    }

    private fun cerrarSesionGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE))
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        client.signOut().addOnCompleteListener {
            // Limpiar SharedPreferences
            getSharedPreferences("datosUsuario", MODE_PRIVATE).edit().clear().apply()

            // Regresar al login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
                        getString(R.string.app_version)
                    }

                    val msg = getString(
                        R.string.dialog_message_version,
                        version,
                        getString(R.string.developer_1),
                        getString(R.string.developer_2),
                        getString(R.string.developer_3),
                        getString(R.string.developer_4),
                        getString(R.string.developer_5)
                    )

                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_title_version))
                        .setMessage(msg)
                        .setPositiveButton(getString(R.string.ok), null)
                        .show()

                    true
                }
                R.id.menu_videos -> {
                    // 🚀 Nuevo acceso directo a los videos
                    startActivity(Intent(this, VideoSelectorActivity::class.java))
                    true
                }
                R.id.menu_logout -> {
                    getSharedPreferences("datosUsuario", MODE_PRIVATE).edit().clear().apply()
                    cerrarSesionGoogle()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
}
