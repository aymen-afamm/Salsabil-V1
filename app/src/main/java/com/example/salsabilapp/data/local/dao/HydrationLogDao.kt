package com.example.salsabilapp.data.local.dao

import androidx.room.*
import com.example.salsabilapp.data.local.entity.HydrationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationLogDao {
    @Query("SELECT * FROM hydration_log WHERE date = :date ORDER BY timestamp DESC")
    fun getLogsForDate(date: String): Flow<List<HydrationLog>>

    @Query("SELECT * FROM hydration_log WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getLogsForDateRange(startDate: String, endDate: String): Flow<List<HydrationLog>>

    @Query("SELECT SUM(amountMl) FROM hydration_log WHERE date = :date")
    fun getTotalForDate(date: String): Flow<Int?>

    @Query("SELECT SUM(amountMl) FROM hydration_log WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalForDateRange(startDate: String, endDate: String): Int?

    @Query("SELECT * FROM hydration_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 10): Flow<List<HydrationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HydrationLog): Long

    @Delete
    suspend fun deleteLog(log: HydrationLog)

    @Query("DELETE FROM hydration_log WHERE date < :beforeDate")
    suspend fun deleteOldLogs(beforeDate: String)
}
