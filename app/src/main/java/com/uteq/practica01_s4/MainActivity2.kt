package com.uteq.practica01_s4

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.uteq.practica01_s4.adapters.AlumnoAdapter
import com.uteq.practica01_s4.data.SupabaseManager
import com.uteq.practica01_s4.models.Alumno
import com.uteq.practica01_s4.models.Materia
import com.uteq.practica01_s4.utils.SupabaseErrorHandler
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity2 : AppCompatActivity() {
    private lateinit var actvNiveles: AutoCompleteTextView
    private lateinit var actvMaterias: AutoCompleteTextView
    private lateinit var menuMaterias: TextInputLayout
    private lateinit var lvAlumnos: ListView
    private lateinit var progressCarga: ProgressBar
    private lateinit var txtResumen: TextView
    private lateinit var txtEstado: TextView

    private val lstMaterias = ArrayList<String>()

    // Evita que la respuesta lenta de un nivel anterior sobrescriba las materias actuales
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

        actvNiveles = findViewById(R.id.actvListaNiveles)
        actvMaterias = findViewById(R.id.actvListaMaterias)
        menuMaterias = findViewById(R.id.menuMaterias)
        lvAlumnos = findViewById(R.id.lvAlumnos)
        progressCarga = findViewById(R.id.progressCarga)
        txtResumen = findViewById(R.id.txtResumen)
        txtEstado = findViewById(R.id.txtEstado)

        configurarNiveles()
        configurarMaterias()
        configurarListaAlumnos()
    }

    // 1. Combo de Semestre con los datos de strings.xml
    private fun configurarNiveles() {
        val listaSemestre = resources.getStringArray(R.array.niveles)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            listaSemestre
        )
        actvNiveles.setAdapter(adapter)

        actvNiveles.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            Toast.makeText(this, "Seleccionaste: $selectedItem", Toast.LENGTH_SHORT).show()
            // La posición empieza en 0 y los niveles en 1
            cargarMaterias(position + 1)
        }
    }

    // 2. Combo de Materias con los datos de Supabase filtrados por nivel
    private fun cargarMaterias(nivel: Int) {
        cargaMaterias?.cancel()
        lstMaterias.clear()
        actvMaterias.setText("", false)
        menuMaterias.isEnabled = false
        menuMaterias.helperText = "Cargando materias..."

        // Al cambiar de semestre se limpia el listado anterior
        lvAlumnos.adapter = null
        txtResumen.text = ""
        txtEstado.text = "Seleccione una materia para ver el listado de estudiantes."

        cargaMaterias = lifecycleScope.launch {
            progressCarga.visibility = View.VISIBLE
            try {
                val listaMaterias = ArrayList(
                    SupabaseManager.client
                        .from("materias")
                        .select {
                            filter {
                                eq("nivel", nivel)
                            }
                            order("nombre", Order.ASCENDING)
                        }
                        .decodeList<Materia>()
                )

                for (materia in listaMaterias) {
                    lstMaterias.add(materia.nombre ?: "")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: RestException) {
                SupabaseErrorHandler.show(this@MainActivity2, e)
                lstMaterias.clear()
            } catch (e: Exception) {
                SupabaseErrorHandler.show(this@MainActivity2, e)
                lstMaterias.clear()
            } finally {
                // Si se canceló porque se eligió otro nivel, la nueva consulta llena el combo
                if (isActive) {
                    val adapter = ArrayAdapter(
                        this@MainActivity2,
                        android.R.layout.simple_spinner_dropdown_item,
                        lstMaterias
                    )
                    actvMaterias.setAdapter(adapter)
                    menuMaterias.isEnabled = lstMaterias.isNotEmpty()
                    menuMaterias.helperText = when (lstMaterias.size) {
                        0 -> "No hay materias registradas en este semestre"
                        1 -> "1 materia disponible"
                        else -> "${lstMaterias.size} materias disponibles"
                    }
                    progressCarga.visibility = View.GONE
                }
            }
        }
    }

    // 3. Al elegir una materia se carga la lista de alumnos
    private fun configurarMaterias() {
        actvMaterias.setOnItemClickListener { parent, _, position, _ ->
            val materia = parent.getItemAtPosition(position).toString()
            Toast.makeText(this, "Seleccionaste: $materia", Toast.LENGTH_SHORT).show()
            cargarAlumnos(materia)
        }
    }

    // 4. ListView personalizado con header y evento click
    private fun configurarListaAlumnos() {
        val header = layoutInflater.inflate(R.layout.ly_header, lvAlumnos, false)
        lvAlumnos.addHeaderView(header, null, false)
        // Mientras no haya estudiantes se muestra el mensaje en lugar de la lista
        lvAlumnos.emptyView = txtEstado

        lvAlumnos.setOnItemClickListener { parent, _, position, _ ->
            // Con header, getItemAtPosition ya descuenta la fila del encabezado
            val alumno = parent.getItemAtPosition(position) as? Alumno ?: return@setOnItemClickListener
            Toast.makeText(
                this,
                "Item Seleccionado: ${alumno.nombres}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun cargarAlumnos(materia: String) {
        var lstAlumnos = ArrayList<Alumno>()
        lifecycleScope.launch {
            progressCarga.visibility = View.VISIBLE
            txtResumen.text = "Consultando..."
            txtEstado.text = "Consultando estudiantes de $materia..."
            try {
                lstAlumnos = ArrayList(
                    SupabaseManager.client
                        .from("estudiantes")
                        .select {
                            order("nombres", Order.ASCENDING)
                        }
                        .decodeList<Alumno>()
                )
                txtEstado.text = "No hay estudiantes registrados."
            } catch (e: CancellationException) {
                throw e
            } catch (e: RestException) {
                txtEstado.text = "No se pudo cargar el listado de estudiantes."
                SupabaseErrorHandler.show(this@MainActivity2, e)
            } catch (e: Exception) {
                txtEstado.text = "No se pudo cargar el listado de estudiantes."
                SupabaseErrorHandler.show(this@MainActivity2, e)
            } finally {
                val adapter = AlumnoAdapter(this@MainActivity2, lstAlumnos)
                lvAlumnos.adapter = adapter
                txtResumen.text = when (lstAlumnos.size) {
                    0 -> ""
                    1 -> "1 registro"
                    else -> "${lstAlumnos.size} registros"
                }
                progressCarga.visibility = View.GONE
            }
        }
    }
}
