package com.app.healthmonitor

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


const val SUPABASE_URL = "https://mjsasefqkpsoozpubfvu.supabase.co"
const val  SUPABASE_API_KEY = "sb_publishable_Vz3coi_RFVuiHI1N_NxHcQ_chg6IMzY"

val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_API_KEY
) {
    install(Postgrest)
}


@Serializable
data class EcgData(
    @SerialName("rr")
    val rr: Int,
    @SerialName("avg_rr")
    val avg_rr: Int,
    @SerialName("bpm")
    val bpm: Int,
    @SerialName("sqi")
    val sqi: Int,
    @SerialName("created_at")
    val created_at: String? = null
)