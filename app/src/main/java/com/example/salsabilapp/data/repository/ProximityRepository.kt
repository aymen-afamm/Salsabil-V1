package com.example.salsabilapp.data.repository

import com.example.salsabilapp.data.local.LedgerManager
import com.example.salsabilapp.data.local.dao.ProximityEventDao
import com.example.salsabilapp.data.local.entity.ProximityEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProximityRepository @Inject constructor(
    private val proximityEventDao: ProximityEventDao,
    private val ledgerManager: LedgerManager
) {
    fun getRecentEvents(limit: Int = 50): Flow<List<ProximityEvent>> {
        return proximityEventDao.getRecentEvents(limit)
    }

    fun getEventsForTimeRange(startTime: Long, endTime: Long): Flow<List<ProximityEvent>> {
        return proximityEventDao.getEventsForTimeRange(startTime, endTime)
    }

    suspend fun getEventCountSince(since: Long): Int {
        return proximityEventDao.getEventCountSince(since)
    }

    suspend fun addProximityEvent(
        ephemeralUuid: String,
        rssi: Int,
        actionTaken: String = "none",
        distance: String = "unknown"
    ): Long {
        val event = ProximityEvent(
            ephemeralUuid = ephemeralUuid,
            rssi = rssi,
            actionTaken = actionTaken,
            distance = distance
        )
        val id = proximityEventDao.insertEvent(event)

        // Add to ledger
        ledgerManager.addEntry(
            LedgerManager.EVENT_PROXIMITY_DETECTED,
            mapOf(
                "ephemeralUuid" to ephemeralUuid,
                "rssi" to rssi,
                "distance" to distance,
                "timestamp" to event.timestamp
            )
        )

        return id
    }

    suspend fun deleteEvent(event: ProximityEvent) {
        proximityEventDao.deleteEvent(event)
    }

    suspend fun deleteOldEvents(daysToKeep: Int = 30) {
        val beforeTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        proximityEventDao.deleteOldEvents(beforeTime)
    }
}
