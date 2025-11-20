package com.example.salsabilapp.data.local.dao

import androidx.room.*
import com.example.salsabilapp.data.local.entity.SmartwatchData
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartwatchDataDao {
    @Query("SELECT * FROM smartwatch_data ORDER BY timestamp DESC LIMIT 1")
    fun getLatestData(): Flow<SmartwatchData?>

    @Query("SELECT * FROM smartwatch_data WHERE deviceMac = :mac ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestDataForDevice(mac: String): SmartwatchData?

    @Query("SELECT * FROM smartwatch_data ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentData(limit: Int = 20): Flow<List<SmartwatchData>>

    @Query("SELECT * FROM smartwatch_data WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getDataForTimeRange(startTime: Long, endTime: Long): Flow<List<SmartwatchData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(data: SmartwatchData): Long

    @Delete
    suspend fun deleteData(data: SmartwatchData)

    @Query("DELETE FROM smartwatch_data WHERE timestamp < :beforeTime")
    suspend fun deleteOldData(beforeTime: Long)
}
