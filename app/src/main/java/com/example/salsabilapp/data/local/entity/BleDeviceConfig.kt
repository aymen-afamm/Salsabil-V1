package com.example.salsabilapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ble_device_config")
data class BleDeviceConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceMac: String,
    val deviceName: String,
    val serviceUuid: String = "",
    val stepsCharUuid: String = "",
    val heartRateCharUuid: String = "",
    val batteryCharUuid: String = "",
    val customCharUuids: String = "", // JSON array of custom UUIDs
    val isActive: Boolean = true,
    val lastConnected: Long = 0
)
