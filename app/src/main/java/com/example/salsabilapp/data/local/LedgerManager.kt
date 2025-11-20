package com.example.salsabilapp.data.local

import com.example.salsabilapp.data.local.dao.LedgerEntryDao
import com.example.salsabilapp.data.local.entity.LedgerEntry
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LedgerManager @Inject constructor(
    private val ledgerEntryDao: LedgerEntryDao,
    private val gson: Gson
) {

    /**
     * Add a new entry to the tamper-evident ledger
     */
    suspend fun addEntry(eventType: String, payload: Any): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val payloadJson = gson.toJson(payload)
            val timestamp = System.currentTimeMillis()

            // Get the previous entry's hash
            val previousEntry = ledgerEntryDao.getLatestEntry()
            val previousHash = previousEntry?.currentHash ?: "GENESIS"

            // Calculate current hash
            val currentHash = calculateHash(previousHash, timestamp, payloadJson)

            val entry = LedgerEntry(
                timestamp = timestamp,
                eventType = eventType,
                payloadJson = payloadJson,
                previousHash = previousHash,
                currentHash = currentHash
            )

            val id = ledgerEntryDao.insertEntry(entry)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verify the integrity of the entire ledger
     */
    suspend fun verifyLedgerIntegrity(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val entries = ledgerEntryDao.getAllEntries()
            var isValid = true
            var previousHash = "GENESIS"

            entries.collect { entryList ->
                for (entry in entryList.reversed()) {
                    // Verify previous hash matches
                    if (entry.previousHash != previousHash) {
                        isValid = false
                        break
                    }

                    // Recalculate and verify current hash
                    val calculatedHash = calculateHash(
                        entry.previousHash,
                        entry.timestamp,
                        entry.payloadJson
                    )

                    if (entry.currentHash != calculatedHash) {
                        isValid = false
                        break
                    }

                    previousHash = entry.currentHash
                }
            }

            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export ledger as JSON string
     */
    suspend fun exportLedger(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val entries = mutableListOf<LedgerEntry>()
            ledgerEntryDao.getAllEntries().collect { entryList ->
                entries.addAll(entryList)
            }
            val json = gson.toJson(entries)
            Result.success(json)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculate SHA-256 hash for ledger entry
     */
    private fun calculateHash(previousHash: String, timestamp: Long, payloadJson: String): String {
        val input = "$previousHash$timestamp$payloadJson"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        // Event types
        const val EVENT_PROXIMITY_DETECTED = "PROXIMITY_DETECTED"
        const val EVENT_SMARTWATCH_SYNC = "SMARTWATCH_SYNC"
        const val EVENT_REMINDER_SENT = "REMINDER_SENT"
        const val EVENT_GOAL_CHANGED = "GOAL_CHANGED"
        const val EVENT_HYDRATION_LOGGED = "HYDRATION_LOGGED"
        const val EVENT_ACTIVITY_LOGGED = "ACTIVITY_LOGGED"
        const val EVENT_PROFILE_UPDATED = "PROFILE_UPDATED"
        const val EVENT_DEVICE_PAIRED = "DEVICE_PAIRED"
    }
}
