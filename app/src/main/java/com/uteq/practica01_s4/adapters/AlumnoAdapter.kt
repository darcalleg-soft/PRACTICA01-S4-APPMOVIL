package com.uteq.practica01_s4.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.uteq.practica01_s4.R
import com.uteq.practica01_s4.models.Alumno

class AlumnoAdapter(context: Context, var alumnos: ArrayList<Alumno>) :
    ArrayAdapter<Alumno>(context, R.layout.item_alumno, alumnos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Reutiliza la fila si ya existe (Row View Recycling)
        val view = convertView ?: LayoutInflater
            .from(context)
            .inflate(R.layout.item_alumno, parent, false)

        val alumno = alumnos[position]

        val imgFoto = view.findViewById<ImageView>(R.id.imgFoto)
        val txtNombre = view.findViewById<TextView>(R.id.txtNombre)
        val txtCorreo = view.findViewById<TextView>(R.id.txtCorreo)
        val txtTelefono = view.findViewById<TextView>(R.id.txtTelefono)

        // Foto desde la URL guardada en Supabase; si no hay o falla, se ve el ícono de persona.
        // Coil cancela la carga anterior cuando la fila se recicla, así no se mezclan fotos.
        imgFoto.load(alumno.foto) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            error(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }

        txtNombre.text = alumno.nombres
        txtCorreo.text = alumno.correo ?: "Sin correo"
        txtTelefono.text = alumno.telefono ?: "Sin teléfono"

        return view
    }
}
