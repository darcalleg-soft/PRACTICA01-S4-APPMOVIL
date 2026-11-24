package com.uteq.practica01_s4

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.uteq.practica01_s4.data.SupabaseManager
import com.uteq.practica01_s4.models.Carrera
import com.uteq.practica01_s4.models.Materia
import com.uteq.practica01_s4.utils.SupabaseErrorHandler
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity2 : AppCompatActivity() {
    private lateinit var spnCarrera: Spinner
    private lateinit var spnMateria: Spinner
    private lateinit var progressCatalogo: ProgressBar
    private lateinit var txtSeleccion: TextView

    private lateinit var carreraAdapter: ArrayAdapter<Carrera>
    private lateinit var materiaAdapter: ArrayAdapter<Materia>

    // Evita que una respuesta lenta de otra carrera sobrescriba la lista actual
    private var cargaMaterias: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

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

        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }

        spnCarrera = findViewById(R.id.spnCarrera)
        spnMateria = findViewById(R.id.spnMateria)
        progressCatalogo = findViewById(R.id.progressCatalogo)
        txtSeleccion = findViewById(R.id.txtSeleccion)

        carreraAdapter = crearAdapter()
        materiaAdapter = crearAdapter()
        spnCarrera.adapter = carreraAdapter
        spnMateria.adapter = materiaAdapter
        spnMateria.isEnabled = false

        spnCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                carreraAdapter.getItem(position)?.let { cargarMaterias(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        spnMateria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mostrarSeleccion()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        cargarCarreras()
    }

    private fun <T> crearAdapter() =
        ArrayAdapter<T>(this, android.R.layout.simple_spinner_item, mutableListOf()).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    private fun cargarCarreras() {
        lifecycleScope.launch {
            progressCatalogo.visibility = View.VISIBLE
            try {
                val carreras = SupabaseManager.client
                    .from("carreras")
                    .select { order("nombre", Order.ASCENDING) }
                    .decodeList<Carrera>()

                carreraAdapter.clear()
                carreraAdapter.addAll(carreras)

                if (carreras.isEmpty()) {
                    txtSeleccion.text = "No hay carreras registradas."
                }
            } catch (e: Exception) {
                txtSeleccion.text = "No se pudieron cargar las carreras."
                SupabaseErrorHandler.show(this@MainActivity2, e)
            } finally {
                progressCatalogo.visibility = View.GONE
            }
        }
    }

    private fun cargarMaterias(carrera: Carrera) {
        cargaMaterias?.cancel()
        materiaAdapter.clear()
        spnMateria.isEnabled = false
        txtSeleccion.text = "Cargando materias de ${carrera.nombre}..."

        cargaMaterias = lifecycleScope.launch {
            progressCatalogo.visibility = View.VISIBLE
            try {
                val materias = SupabaseManager.client
                    .from("materias")
                    .select {
                        filter { eq("carrera_id", carrera.id) }
                        order("semestre", Order.ASCENDING)
                        order("nombre", Order.ASCENDING)
                    }
                    .decodeList<Materia>()

                materiaAdapter.addAll(materias)
                spnMateria.isEnabled = materias.isNotEmpty()

                if (materias.isEmpty()) {
                    txtSeleccion.text = "La carrera ${carrera.nombre} no tiene materias registradas."
                } else {
                    spnMateria.setSelection(0)
                    mostrarSeleccion()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                txtSeleccion.text = "No se pudieron cargar las materias."
                SupabaseErrorHandler.show(this@MainActivity2, e)
            } finally {
                // Si se canceló porque se eligió otra carrera, la nueva carga maneja el progreso
                if (isActive) progressCatalogo.visibility = View.GONE
            }
        }
    }

    private fun mostrarSeleccion() {
        val carrera = spnCarrera.selectedItem as? Carrera ?: return
        val materia = spnMateria.selectedItem as? Materia ?: return
        txtSeleccion.text = "Carrera: ${carrera.nombre}\nMateria: $materia"
    }
}
