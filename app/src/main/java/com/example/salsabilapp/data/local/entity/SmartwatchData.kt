package com.example.salsabilapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smartwatch_data")
data class SmartwatchData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceMac: String,
    val deviceName: String = "Balancs Edge",
    val steps: Int = 0,
    val heartRate: Int = 0,
    val batteryLevel: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: String = "synced", // synced, pending, failed
    val rawData: String = "" // JSON string for additional metrics
)
