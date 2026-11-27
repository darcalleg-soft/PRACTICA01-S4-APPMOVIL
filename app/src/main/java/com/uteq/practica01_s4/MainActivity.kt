package com.uteq.practica01_s4

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.uteq.practica01_s4.adapters.AlumnoAdapter
import com.uteq.practica01_s4.data.SupabaseManager
import com.uteq.practica01_s4.models.Alumno
import com.uteq.practica01_s4.utils.SupabaseErrorHandler
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var txtAlumnos: TextView
    private lateinit var progressMaterias: ProgressBar
    private lateinit var lvEstudiantes: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Igual que en MainActivity2: evita que el contenido quede bajo las barras del sistema
        val root = findViewById<View>(R.id.main)
        val basePadding = root.paddingLeft
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                basePadding + systemBars.left,
                basePadding + systemBars.top,
                basePadding + systemBars.right,
                basePadding + systemBars.bottom
            )
            insets
        }

        txtAlumnos = findViewById(R.id.txtAlumnos)
        progressMaterias = findViewById(R.id.progressMaterias)
        lvEstudiantes = findViewById(R.id.lvEstudiantes)

        findViewById<Button>(R.id.btnConsultar)
            .setOnClickListener { consultarAlumnos() }

        consultarAlumnos()
    }

    private fun consultarAlumnos() {
        lifecycleScope.launch {
            progressMaterias.visibility = View.VISIBLE
            txtAlumnos.text = "Consultando estudiantes..."

            try {
                val alumnos = SupabaseManager.client
                    .from("estudiantes")
                    .select {
                        order("nombres", Order.ASCENDING)
                    }
                    .decodeList<Alumno>()

                lvEstudiantes.adapter = AlumnoAdapter(this@MainActivity, ArrayList(alumnos))

                txtAlumnos.text = if (alumnos.isEmpty()) {
                    "No se encontraron estudiantes."
                } else {
                    "${alumnos.size} estudiantes registrados."
                }
            } catch (e: Exception) {
                txtAlumnos.text = "No se pudo cargar la lista."
                SupabaseErrorHandler.show(this@MainActivity, e)
            } finally {
                progressMaterias.visibility = View.GONE
            }
        }
    }
}
