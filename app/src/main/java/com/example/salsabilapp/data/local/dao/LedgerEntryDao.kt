package com.example.salsabilapp.data.local.dao

import androidx.room.*
import com.example.salsabilapp.data.local.entity.LedgerEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerEntryDao {
    @Query("SELECT * FROM ledger_entry ORDER BY id DESC")
    fun getAllEntries(): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entry ORDER BY id DESC LIMIT 1")
    suspend fun getLatestEntry(): LedgerEntry?

    @Query("SELECT * FROM ledger_entry WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getEntriesByType(eventType: String): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entry WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEntriesForTimeRange(startTime: Long, endTime: Long): Flow<List<LedgerEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LedgerEntry): Long

    @Query("SELECT COUNT(*) FROM ledger_entry")
    suspend fun getEntryCount(): Int

    @Query("DELETE FROM ledger_entry")
    suspend fun deleteAllEntries()
}
