package com.example.salsabilapp.domain.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UuidScanner @Inject constructor(
    private val bleManager: BleManager
) {

    /**
     * Scan all available services and characteristics from connected device
     */
    fun scanAllUuids(): List<ServiceInfo> {
        val services = bleManager.getAvailableServices()
        val serviceInfoList = mutableListOf<ServiceInfo>()

        for (service in services) {
            val characteristics = mutableListOf<CharacteristicInfo>()

            for (characteristic in service.characteristics) {
                characteristics.add(
                    CharacteristicInfo(
                        uuid = characteristic.uuid.toString(),
                        properties = getPropertiesString(characteristic),
                        permissions = getPermissionsString(characteristic)
                    )
                )
            }

            serviceInfoList.add(
                ServiceInfo(
                    uuid = service.uuid.toString(),
                    type = getServiceTypeString(service.type),
                    characteristics = characteristics
                )
            )
        }

        return serviceInfoList
    }

    /**
     * Get human-readable properties string
     */
    private fun getPropertiesString(characteristic: BluetoothGattCharacteristic): String {
        val properties = mutableListOf<String>()

        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
            properties.add("READ")
        }
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
            properties.add("WRITE")
        }
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
            properties.add("NOTIFY")
        }
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
            properties.add("INDICATE")
        }
        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
            properties.add("WRITE_NO_RESPONSE")
        }

        return properties.joinToString(", ")
    }

    /**
     * Get human-readable permissions string
     */
    private fun getPermissionsString(characteristic: BluetoothGattCharacteristic): String {
        val permissions = mutableListOf<String>()

        if (characteristic.permissions and BluetoothGattCharacteristic.PERMISSION_READ != 0) {
            permissions.add("READ")
        }
        if (characteristic.permissions and BluetoothGattCharacteristic.PERMISSION_WRITE != 0) {
            permissions.add("WRITE")
        }

        return permissions.joinToString(", ")
    }

    /**
     * Get service type string
     */
    private fun getServiceTypeString(type: Int): String {
        return when (type) {
            BluetoothGattService.SERVICE_TYPE_PRIMARY -> "PRIMARY"
            BluetoothGattService.SERVICE_TYPE_SECONDARY -> "SECONDARY"
            else -> "UNKNOWN"
        }
    }

    /**
     * Get known service name from UUID
     */
    fun getKnownServiceName(uuid: String): String {
        return when (uuid.lowercase()) {
            "0000180d-0000-1000-8000-00805f9b34fb" -> "Heart Rate Service"
            "0000180f-0000-1000-8000-00805f9b34fb" -> "Battery Service"
            "00001800-0000-1000-8000-00805f9b34fb" -> "Generic Access"
            "00001801-0000-1000-8000-00805f9b34fb" -> "Generic Attribute"
            "0000180a-0000-1000-8000-00805f9b34fb" -> "Device Information"
            "00001805-0000-1000-8000-00805f9b34fb" -> "Current Time Service"
            else -> "Custom Service"
        }
    }

    /**
     * Get known characteristic name from UUID
     */
    fun getKnownCharacteristicName(uuid: String): String {
        return when (uuid.lowercase()) {
            "00002a37-0000-1000-8000-00805f9b34fb" -> "Heart Rate Measurement"
            "00002a19-0000-1000-8000-00805f9b34fb" -> "Battery Level"
            "00002a29-0000-1000-8000-00805f9b34fb" -> "Manufacturer Name"
            "00002a24-0000-1000-8000-00805f9b34fb" -> "Model Number"
            "00002a25-0000-1000-8000-00805f9b34fb" -> "Serial Number"
            "00002a27-0000-1000-8000-00805f9b34fb" -> "Hardware Revision"
            "00002a26-0000-1000-8000-00805f9b34fb" -> "Firmware Revision"
            "00002a28-0000-1000-8000-00805f9b34fb" -> "Software Revision"
            else -> "Custom Characteristic"
        }
    }

    data class ServiceInfo(
        val uuid: String,
        val type: String,
        val characteristics: List<CharacteristicInfo>
    )

    data class CharacteristicInfo(
        val uuid: String,
        val properties: String,
        val permissions: String
    )
}
