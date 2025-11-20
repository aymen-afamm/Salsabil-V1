package com.example.salsabilapp.presentation.proximity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salsabilapp.data.repository.ProximityRepository
import com.example.salsabilapp.domain.ble.ProximityScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProximityViewModel @Inject constructor(
    private val proximityScanner: ProximityScanner,
    private val proximityRepository: ProximityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProximityUiState())
    val uiState: StateFlow<ProximityUiState> = _uiState.asStateFlow()

    val nearbyUsers = proximityScanner.nearbyUsers

    fun enableProximityMode() {
        proximityScanner.startAdvertising()
        proximityScanner.startScanning()
        _uiState.value = _uiState.value.copy(isEnabled = true)
    }

    fun disableProximityMode() {
        proximityScanner.stopAdvertising()
        proximityScanner.stopScanning()
        _uiState.value = _uiState.value.copy(isEnabled = false)
    }

    fun loadRecentEvents() {
        viewModelScope.launch {
            proximityRepository.getRecentEvents(50).collect { events ->
                _uiState.value = _uiState.value.copy(
                    recentEventCount = events.size,
                    isLoading = false
                )
            }
        }
    }
}

data class ProximityUiState(
    val isEnabled: Boolean = false,
    val recentEventCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)
