package com.uteq.practica01_s4.models

import kotlinx.serialization.Serializable

@Serializable
data class Carrera(
    val id: Long,
    val nombre: String
) {
    // El Spinner muestra el resultado de toString()
    override fun toString(): String = nombre
}
