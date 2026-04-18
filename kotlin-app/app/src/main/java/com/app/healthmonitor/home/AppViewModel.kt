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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import kotlin.math.log
import kotlin.random.Random


class AppViewModel : ViewModel() {
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
                val isLive: StateFlow<Boolean> = _uiState
                    .map { data ->
                        val supabaseTime = try {
                            data.created_at?.let { Instant.parse(it) }
                        } catch (e: Exception) {
                            null
                        }

                        val currentTime = Instant.now()
                        val timeDiff = supabaseTime?.let {
                            Duration.between(it, currentTime).abs()
                        }

                        data.bpm != 0 &&
                                timeDiff != null &&
                                timeDiff < Duration.ofSeconds(30)
                    }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = false
                    )
                _isLiveSync.value = true

            }
        }
    }
    fun getData() {
        viewModelScope.launch {
            try {
                val latest = supabase
                    .from("ecg")
                    .select {
                        limit(5)
                        order("created_at", order = Order.DESCENDING)
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

}