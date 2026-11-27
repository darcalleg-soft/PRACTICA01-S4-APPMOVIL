package com.uteq.practica01_s4.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.jan.supabase.exceptions.RestException

object SupabaseErrorHandler {

    // Error devuelto por Supabase (tabla inexistente, RLS, clave inválida...)
    fun show(context: Context, e: RestException) {
        MaterialAlertDialogBuilder(context)
            .setTitle(e.error)
            .setMessage(e.description)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    // Errores de red: sin internet, DNS o tiempo de espera agotado
    fun show(context: Context, error: Throwable) {
        if (error is RestException) {
            show(context, error)
            return
        }

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
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Aceptar", null)
            .show()
    }
}
