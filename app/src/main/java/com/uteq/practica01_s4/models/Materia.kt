package com.uteq.practica01_s4.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Materia(
    val id: Long,
    val nombre: String,
    @SerialName("carrera_id") val carreraId: Long,
    val semestre: Int? = null
) {
    // El Spinner muestra el resultado de toString()
    override fun toString(): String =
        if (semestre != null) "$nombre (Semestre $semestre)" else nombre
}
