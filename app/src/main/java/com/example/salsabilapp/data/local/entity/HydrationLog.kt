package com.example.salsabilapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_log")
data class HydrationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // Format: yyyy-MM-dd for grouping
    val note: String = ""
)
