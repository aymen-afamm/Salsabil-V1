package com.example.salsabilapp.data.local.dao

import androidx.room.*
import com.example.salsabilapp.data.local.entity.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_log WHERE date = :date ORDER BY timestamp DESC")
    fun getLogsForDate(date: String): Flow<List<ActivityLog>>

    @Query("SELECT SUM(steps) FROM activity_log WHERE date = :date")
    fun getTotalStepsForDate(date: String): Flow<Int?>

    @Query("SELECT SUM(distanceMeters) FROM activity_log WHERE date = :date")
    suspend fun getTotalDistanceForDate(date: String): Float?

    @Query("SELECT * FROM activity_log WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getLogsForDateRange(startDate: String, endDate: String): Flow<List<ActivityLog>>

    @Query("SELECT SUM(steps) FROM activity_log WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalStepsForDateRange(startDate: String, endDate: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long

    @Delete
    suspend fun deleteLog(log: ActivityLog)

    @Query("DELETE FROM activity_log WHERE date < :beforeDate")
    suspend fun deleteOldLogs(beforeDate: String)
}
