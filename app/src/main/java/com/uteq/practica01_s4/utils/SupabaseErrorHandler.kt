package com.uteq.practica01_s4.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object SupabaseErrorHandler {
    fun show(context: Context, error: Throwable) {
        val esTimeout = generateSequence(error) { it.cause }
            .any { it::class.java.simpleName.contains("Timeout", ignoreCase = true) }

        val detail = if (esTimeout) {
            "El servidor no respondió a tiempo. Verifique la conexión a Internet e intente de nuevo."
        } else {
            error.localizedMessage ?: "No se pudo completar la consulta."
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("Error de conexión")
            .setMessage(detail)
            .setPositiveButton("Aceptar", null)
            .show()
    }
}
