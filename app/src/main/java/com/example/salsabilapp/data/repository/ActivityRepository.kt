package com.example.salsabilapp.data.repository

import com.example.salsabilapp.data.local.LedgerManager
import com.example.salsabilapp.data.local.dao.ActivityLogDao
import com.example.salsabilapp.data.local.entity.ActivityLog
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val activityLogDao: ActivityLogDao,
    private val ledgerManager: LedgerManager
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getLogsForDate(date: String): Flow<List<ActivityLog>> {
        return activityLogDao.getLogsForDate(date)
    }

    fun getLogsForToday(): Flow<List<ActivityLog>> {
        val today = dateFormat.format(Date())
        return activityLogDao.getLogsForDate(today)
    }

    fun getTotalStepsForDate(date: String): Flow<Int?> {
        return activityLogDao.getTotalStepsForDate(date)
    }

    fun getTotalStepsForToday(): Flow<Int?> {
        val today = dateFormat.format(Date())
        return activityLogDao.getTotalStepsForDate(today)
    }

    suspend fun getTotalStepsForDateRange(startDate: String, endDate: String): Int {
        return activityLogDao.getTotalStepsForDateRange(startDate, endDate) ?: 0
    }

    suspend fun getTotalDistanceForDate(date: String): Float {
        return activityLogDao.getTotalDistanceForDate(date) ?: 0f
    }

    suspend fun addActivityLog(
        activityType: String,
        steps: Int,
        distanceMeters: Float,
        durationMinutes: Int,
        source: String = "phone"
    ): Long {
        val date = dateFormat.format(Date())
        val log = ActivityLog(
            activityType = activityType,
            steps = steps,
            distanceMeters = distanceMeters,
            durationMinutes = durationMinutes,
            date = date,
            source = source
        )
        val id = activityLogDao.insertLog(log)

        // Add to ledger
        ledgerManager.addEntry(
            LedgerManager.EVENT_ACTIVITY_LOGGED,
            mapOf(
                "activityType" to activityType,
                "steps" to steps,
                "source" to source,
                "timestamp" to log.timestamp
            )
        )

        return id
    }

    suspend fun deleteLog(log: ActivityLog) {
        activityLogDao.deleteLog(log)
    }

    suspend fun deleteOldLogs(daysToKeep: Int = 90) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysToKeep)
        val beforeDate = dateFormat.format(calendar.time)
        activityLogDao.deleteOldLogs(beforeDate)
    }
}
