package com.example.salsabilapp.domain.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProximityScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var advertiser: BluetoothLeAdvertiser? = null
    private var scanner: BluetoothLeScanner? = null

    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers.asStateFlow()

    private var currentEphemeralUuid: String = generateEphemeralUuid()
    private var isAdvertising = false
    private var isScanning = false

    /**
     * Start advertising as a Salsabil user (Kidney Support Mode)
     */
    @SuppressLint("MissingPermission")
    fun startAdvertising() {
        if (isAdvertising) return

        advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        if (advertiser == null) {
            Log.e(TAG, "Bluetooth LE Advertiser not available")
            return
        }

        // Rotate UUID for privacy
        currentEphemeralUuid = generateEphemeralUuid()

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(UUID.fromString(currentEphemeralUuid)))
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)
        isAdvertising = true
    }

    /**
     * Stop advertising
     */
    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (!isAdvertising) return
        advertiser?.stopAdvertising(advertiseCallback)
        isAdvertising = false
    }

    /**
     * Start scanning for nearby Salsabil users
     */
    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (isScanning) return

        scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            Log.e(TAG, "Bluetooth LE Scanner not available")
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        // Scan for Salsabil service UUID pattern
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(SALSABIL_BASE_UUID)))
                .build()
        )

        scanner?.startScan(filters, settings, scanCallback)
        isScanning = true
    }

    /**
     * Stop scanning
     */
    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!isScanning) return
        scanner?.stopScan(scanCallback)
        isScanning = false
    }

    /**
     * Rotate ephemeral UUID for privacy (should be called periodically)
     */
    @SuppressLint("MissingPermission")
    fun rotateUuid() {
        if (isAdvertising) {
            stopAdvertising()
            startAdvertising()
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Advertising failed with error: $errorCode")
            isAdvertising = false
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val serviceUuids = result.scanRecord?.serviceUuids
            if (serviceUuids != null && serviceUuids.isNotEmpty()) {
                val uuid = serviceUuids[0].uuid.toString()

                // Check if it's a Salsabil UUID
                if (isSalsabilUuid(uuid)) {
                    val nearbyUser = NearbyUser(
                        ephemeralUuid = uuid,
                        rssi = result.rssi,
                        distance = calculateDistance(result.rssi),
                        lastSeen = System.currentTimeMillis()
                    )

                    val currentList = _nearbyUsers.value.toMutableList()
                    val existingIndex = currentList.indexOfFirst { it.ephemeralUuid == uuid }

                    if (existingIndex >= 0) {
                        currentList[existingIndex] = nearbyUser
                    } else {
                        currentList.add(nearbyUser)
                    }

                    _nearbyUsers.value = currentList
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error: $errorCode")
            isScanning = false
        }
    }

    /**
     * Generate ephemeral UUID for privacy
     */
    private fun generateEphemeralUuid(): String {
        // Generate UUID based on Salsabil base UUID with random component
        val random = UUID.randomUUID().toString().substring(0, 8)
        return "$random-${SALSABIL_BASE_UUID.substring(9)}"
    }

    /**
     * Check if UUID matches Salsabil pattern
     */
    private fun isSalsabilUuid(uuid: String): Boolean {
        // Check if UUID ends with Salsabil base pattern
        return uuid.endsWith(SALSABIL_BASE_UUID.substring(9))
    }

    /**
     * Calculate approximate distance from RSSI
     */
    private fun calculateDistance(rssi: Int): String {
        return when {
            rssi > -50 -> "immediate" // < 0.5m
            rssi > -70 -> "near"      // 0.5m - 3m
            rssi > -90 -> "far"       // 3m - 10m
            else -> "very_far"        // > 10m
        }
    }

    /**
     * Clean up old nearby users (not seen in last 30 seconds)
     */
    fun cleanupOldUsers() {
        val now = System.currentTimeMillis()
        val threshold = 30_000L // 30 seconds

        _nearbyUsers.value = _nearbyUsers.value.filter {
            now - it.lastSeen < threshold
        }
    }

    companion object {
        private const val TAG = "ProximityScanner"

        // Base UUID for Salsabil app (privacy-preserving)
        // Format: xxxxxxxx-7a1b-11ef-b864-0242ac120002
        private const val SALSABIL_BASE_UUID = "00000000-7a1b-11ef-b864-0242ac120002"
    }

    data class NearbyUser(
        val ephemeralUuid: String,
        val rssi: Int,
        val distance: String,
        val lastSeen: Long
    )
}
