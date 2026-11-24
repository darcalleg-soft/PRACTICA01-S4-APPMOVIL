package com.uteq.practica01_s4.data

import com.uteq.practica01_s4.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp
import kotlin.time.Duration.Companion.seconds

object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        // OkHttp cancela la llamada aunque esté bloqueada resolviendo DNS,
        // así el timeout se cumple y la app muestra el error en vez de cargar sin fin.
        httpEngine = OkHttp.create()
        requestTimeout = 15.seconds
        install(Postgrest)
    }
}
