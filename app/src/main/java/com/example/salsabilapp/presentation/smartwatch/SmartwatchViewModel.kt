package com.example.salsabilapp.presentation.smartwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salsabilapp.data.local.entity.BleDeviceConfig
import com.example.salsabilapp.data.repository.SmartwatchRepository
import com.example.salsabilapp.domain.ble.BleManager
import com.example.salsabilapp.domain.ble.UuidScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartwatchViewModel @Inject constructor(
    private val bleManager: BleManager,
    private val uuidScanner: UuidScanner,
    private val smartwatchRepository: SmartwatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartwatchUiState())
    val uiState: StateFlow<SmartwatchUiState> = _uiState.asStateFlow()

    val scannedDevices = bleManager.scannedDevices
    val connectionState = bleManager.connectionState
    val smartwatchData = bleManager.smartwatchData

    init {
        loadActiveDevice()
    }

    private fun loadActiveDevice() {
        viewModelScope.launch {
            smartwatchRepository.getActiveDevice().collect { device ->
                _uiState.value = _uiState.value.copy(
                    activeDevice = device,
                    isLoading = false
                )
            }
        }
    }

    fun startScan() {
        _uiState.value = _uiState.value.copy(isScanning = true)
        bleManager.startScan()
    }

    fun stopScan() {
        bleManager.stopScan()
        _uiState.value = _uiState.value.copy(isScanning = false)
    }

    fun connectToDevice(deviceAddress: String) {
        bleManager.connectToDevice(deviceAddress)
    }

    fun disconnect() {
        bleManager.disconnect()
    }

    fun scanUuids() {
        val services = uuidScanner.scanAllUuids()
        _uiState.value = _uiState.value.copy(
            discoveredServices = services,
            showUuidScanner = true
        )
    }

    fun saveDeviceConfig(
        deviceMac: String,
        deviceName: String,
        serviceUuid: String,
        stepsCharUuid: String,
        heartRateCharUuid: String,
        batteryCharUuid: String
    ) {
        viewModelScope.launch {
            val config = BleDeviceConfig(
                deviceMac = deviceMac,
                deviceName = deviceName,
                serviceUuid = serviceUuid,
                stepsCharUuid = stepsCharUuid,
                heartRateCharUuid = heartRateCharUuid,
                batteryCharUuid = batteryCharUuid,
                isActive = true,
                lastConnected = System.currentTimeMillis()
            )

            smartwatchRepository.saveDeviceConfig(config)
            _uiState.value = _uiState.value.copy(configSaved = true)
        }
    }

    fun hideUuidScanner() {
        _uiState.value = _uiState.value.copy(showUuidScanner = false)
    }
}

data class SmartwatchUiState(
    val activeDevice: BleDeviceConfig? = null,
    val isScanning: Boolean = false,
    val isLoading: Boolean = true,
    val discoveredServices: List<UuidScanner.ServiceInfo> = emptyList(),
    val showUuidScanner: Boolean = false,
    val configSaved: Boolean = false,
    val error: String? = null
)
