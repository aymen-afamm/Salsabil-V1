package com.example.salsabilapp.data.repository

import com.example.salsabilapp.data.local.LedgerManager
import com.example.salsabilapp.data.local.dao.HydrationLogDao
import com.example.salsabilapp.data.local.entity.HydrationLog
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HydrationRepository @Inject constructor(
    private val hydrationLogDao: HydrationLogDao,
    private val ledgerManager: LedgerManager
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getLogsForDate(date: String): Flow<List<HydrationLog>> {
        return hydrationLogDao.getLogsForDate(date)
    }

    fun getLogsForToday(): Flow<List<HydrationLog>> {
        val today = dateFormat.format(Date())
        return hydrationLogDao.getLogsForDate(today)
    }

    fun getTotalForDate(date: String): Flow<Int?> {
        return hydrationLogDao.getTotalForDate(date)
    }

    fun getTotalForToday(): Flow<Int?> {
        val today = dateFormat.format(Date())
        return hydrationLogDao.getTotalForDate(today)
    }

    suspend fun getTotalForDateRange(startDate: String, endDate: String): Int {
        return hydrationLogDao.getTotalForDateRange(startDate, endDate) ?: 0
    }

    fun getRecentLogs(limit: Int = 10): Flow<List<HydrationLog>> {
        return hydrationLogDao.getRecentLogs(limit)
    }

    suspend fun addHydrationLog(amountMl: Int, note: String = ""): Long {
        val date = dateFormat.format(Date())
        val log = HydrationLog(
            amountMl = amountMl,
            date = date,
            note = note
        )
        val id = hydrationLogDao.insertLog(log)

        // Add to ledger
        ledgerManager.addEntry(
            LedgerManager.EVENT_HYDRATION_LOGGED,
            mapOf("amountMl" to amountMl, "timestamp" to log.timestamp)
        )

        return id
    }

    suspend fun deleteLog(log: HydrationLog) {
        hydrationLogDao.deleteLog(log)
    }

    suspend fun deleteOldLogs(daysToKeep: Int = 90) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysToKeep)
        val beforeDate = dateFormat.format(calendar.time)
        hydrationLogDao.deleteOldLogs(beforeDate)
    }
}
