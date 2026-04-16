package com.app.healthmonitor.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.healthmonitor.EcgData
import com.app.healthmonitor.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.serializer.UnixTimestampSerializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import kotlin.math.log
import kotlin.random.Random


data class DiceUiState(
    val firstDieValue: Int? = null,
    val secondDieValue: Int? = null,
    val numberOfRolls: Int = 0,
)
class AppViewModel : ViewModel() {
    // Expose screen UI state
    private val _uiState = MutableStateFlow(EcgData(
        0,0,0,0
    ))
    val uiState: StateFlow<EcgData> = _uiState.asStateFlow()
    private val _isLiveSync = MutableStateFlow(false)
    val  isLiveSync: StateFlow<Boolean> = _isLiveSync.asStateFlow()


    fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                getData()
                delay(2000)
                val supabaseTime = if (uiState.value.created_at != null) Instant.parse(uiState.value.created_at) else null
                val currentTime = Instant.now()
                val timeDifference = if (supabaseTime == null) null else Duration.between(supabaseTime,currentTime).abs()
                Log.d("TIME DIFFERENCE",timeDifference.toString())

                if (uiState.value.bpm != 0 && timeDifference != null && timeDifference < Duration.ofSeconds(30)) {
                    _isLiveSync.update{ true }
                } else _isLiveSync.update{ false }
                Log.d("is LIVE?",isLiveSync.toString())

            }
        }
    }
    fun getData() {
        viewModelScope.launch {
            try {
                val latest = supabase
                    .from("ecg")
                    .select {
                        limit(1)
                        order("created_at", order = Order.DESCENDING    )
                    }
                    .decodeSingle<EcgData>()
                Log.d("ECG DATA",latest.toString())


                _uiState.update {
                    it.copy(
                        bpm = latest.bpm,
                        rr = latest.rr,
                        avg_rr = latest.avg_rr,
                        sqi = latest.sqi
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun sendData(){

    }

}