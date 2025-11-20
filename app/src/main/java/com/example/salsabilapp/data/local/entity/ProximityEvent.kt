package com.example.salsabilapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proximity_event")
data class ProximityEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ephemeralUuid: String,
    val rssi: Int, // Signal strength
    val timestamp: Long = System.currentTimeMillis(),
    val actionTaken: String = "none", // none, notified, message_sent
    val distance: String = "unknown" // near, far, immediate
)
