package com.example.salsabilapp.data.local.dao

import androidx.room.*
import com.example.salsabilapp.data.local.entity.ProximityEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface ProximityEventDao {
    @Query("SELECT * FROM proximity_event ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 50): Flow<List<ProximityEvent>>

    @Query("SELECT * FROM proximity_event WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEventsForTimeRange(startTime: Long, endTime: Long): Flow<List<ProximityEvent>>

    @Query("SELECT COUNT(*) FROM proximity_event WHERE timestamp > :since")
    suspend fun getEventCountSince(since: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ProximityEvent): Long

    @Delete
    suspend fun deleteEvent(event: ProximityEvent)

    @Query("DELETE FROM proximity_event WHERE timestamp < :beforeTime")
    suspend fun deleteOldEvents(beforeTime: Long)
}
