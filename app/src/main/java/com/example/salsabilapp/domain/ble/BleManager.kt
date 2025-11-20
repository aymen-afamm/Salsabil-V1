package com.example.salsabilapp.domain.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var bluetoothGatt: BluetoothGatt? = null
    private var currentDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices.asStateFlow()

    private val _smartwatchData = MutableStateFlow<SmartwatchData?>(null)
    val smartwatchData: StateFlow<SmartwatchData?> = _smartwatchData.asStateFlow()

    private var isScanning = false

    @SuppressLint("MissingPermission")
    fun startScan(onDeviceFound: ((ScannedDevice) -> Unit)? = null) {
        if (isScanning) return

        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = ScannedDevice(
                    name = result.device.name ?: "Unknown",
                    address = result.device.address,
                    rssi = result.rssi
                )

                val currentList = _scannedDevices.value.toMutableList()
                if (!currentList.any { it.address == device.address }) {
                    currentList.add(device)
                    _scannedDevices.value = currentList
                    onDeviceFound?.invoke(device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "Scan failed with error: $errorCode")
            }
        }

        scanner.startScan(null, scanSettings, scanCallback)
        isScanning = true
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(object : ScanCallback() {})
        isScanning = false
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String) {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress) ?: return
        currentDevice = device
        _connectionState.value = ConnectionState.CONNECTING

        bluetoothGatt = device.connectGatt(context, true, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristic(serviceUuid: String, characteristicUuid: String) {
        val service = bluetoothGatt?.getService(UUID.fromString(serviceUuid)) ?: return
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuid)) ?: return
        bluetoothGatt?.readCharacteristic(characteristic)
    }

    @SuppressLint("MissingPermission")
    fun enableNotifications(serviceUuid: String, characteristicUuid: String) {
        val service = bluetoothGatt?.getService(UUID.fromString(serviceUuid)) ?: return
        val characteristic = service.getCharacteristic(UUID.fromString(characteristicUuid)) ?: return

        bluetoothGatt?.setCharacteristicNotification(characteristic, true)

        // Enable notifications on the device
        val descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt?.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            bluetoothGatt?.writeDescriptor(descriptor)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.CONNECTED
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = ConnectionState.READY
                Log.d(TAG, "Services discovered: ${gatt.services.size}")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                parseCharacteristicData(characteristic.uuid.toString(), value)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                @Suppress("DEPRECATION")
                parseCharacteristicData(characteristic.uuid.toString(), characteristic.value)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            parseCharacteristicData(characteristic.uuid.toString(), value)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            parseCharacteristicData(characteristic.uuid.toString(), characteristic.value)
        }
    }

    private fun parseCharacteristicData(uuid: String, data: ByteArray) {
        // Parse data based on characteristic UUID
        // This is a placeholder - actual parsing depends on device specifications
        Log.d(TAG, "Characteristic $uuid data: ${data.contentToString()}")
    }

    fun getAvailableServices(): List<BluetoothGattService> {
        return bluetoothGatt?.services ?: emptyList()
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    companion object {
        private const val TAG = "BleManager"
        private const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

        // Standard BLE Service UUIDs
        const val HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb"
        const val HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"
        const val BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb"
        const val BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb"
    }

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        READY
    }

    data class ScannedDevice(
        val name: String,
        val address: String,
        val rssi: Int
    )

    data class SmartwatchData(
        val steps: Int = 0,
        val heartRate: Int = 0,
        val batteryLevel: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    )
}
