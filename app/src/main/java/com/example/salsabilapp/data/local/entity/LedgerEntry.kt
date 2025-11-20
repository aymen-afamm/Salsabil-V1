package com.example.salsabilapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_entry")
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // PROXIMITY_DETECTED, SMARTWATCH_SYNC, REMINDER_SENT, GOAL_CHANGED, etc.
    val payloadJson: String, // JSON string containing event details
    val previousHash: String = "",
    val currentHash: String = ""
)
