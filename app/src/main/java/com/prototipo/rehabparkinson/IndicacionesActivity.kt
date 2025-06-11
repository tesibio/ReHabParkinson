package com.prototipo.rehabparkinson

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity

class IndicacionesActivity : AppCompatActivity() {

    private lateinit var btnSiguiente: Button
    private lateinit var btnSalir: Button
    private lateinit var checkBoxes: List<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_indicaciones)

        btnSiguiente = findViewById(R.id.btnSiguiente)
        btnSalir = findViewById(R.id.btnSalir)
        val tipoEjercicio = intent.getStringExtra("tipoEjercicio") ?: "CE" // por defecto CE


        // Inicializamos todos los CheckBoxes con sus IDs correctos
        checkBoxes = listOf(
            findViewById(R.id.cbChkbx1),
            findViewById(R.id.cbChkbx2),
            findViewById(R.id.cbChkbx3),
            findViewById(R.id.cbChkbx4),
            findViewById(R.id.cbChkbx5),
            findViewById(R.id.cbChkbx6)
        )

        // Botón deshabilitado al inicio
        btnSiguiente.isEnabled = false

        // Listener común para todos los CheckBoxes
        val listener = CompoundButton.OnCheckedChangeListener { _, _ ->
            btnSiguiente.isEnabled = checkBoxes.all { it.isChecked }
        }

        // Asignar el listener a todos los CheckBoxes
        checkBoxes.forEach { it.setOnCheckedChangeListener(listener) }

        // Acción del botón Siguiente
        btnSiguiente.setOnClickListener {
            //val intent = Intent(this, ExerciseCameraActivity::class.java)
            val intent = Intent(this, VideoCaptureActivity::class.java)
            intent.putExtra("tipoEjercicio", tipoEjercicio)
            startActivity(intent)
        }

        // Acción del botón Salir
        btnSalir.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
