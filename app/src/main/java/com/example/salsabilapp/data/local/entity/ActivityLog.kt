package com.example.salsabilapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_log")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityType: String, // WALKING, RUNNING, STILL, IN_VEHICLE, etc.
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val durationMinutes: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // Format: yyyy-MM-dd
    val source: String = "phone" // phone, smartwatch
)
