package com.example.salsabilapp.data.repository

import com.example.salsabilapp.data.local.LedgerManager
import com.example.salsabilapp.data.local.dao.BleDeviceConfigDao
import com.example.salsabilapp.data.local.dao.SmartwatchDataDao
import com.example.salsabilapp.data.local.entity.BleDeviceConfig
import com.example.salsabilapp.data.local.entity.SmartwatchData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartwatchRepository @Inject constructor(
    private val smartwatchDataDao: SmartwatchDataDao,
    private val bleDeviceConfigDao: BleDeviceConfigDao,
    private val ledgerManager: LedgerManager
) {
    fun getLatestData(): Flow<SmartwatchData?> {
        return smartwatchDataDao.getLatestData()
    }

    suspend fun getLatestDataForDevice(mac: String): SmartwatchData? {
        return smartwatchDataDao.getLatestDataForDevice(mac)
    }

    fun getRecentData(limit: Int = 20): Flow<List<SmartwatchData>> {
        return smartwatchDataDao.getRecentData(limit)
    }

    suspend fun saveSmartwatchData(data: SmartwatchData): Long {
        val id = smartwatchDataDao.insertData(data)

        // Add to ledger
        ledgerManager.addEntry(
            LedgerManager.EVENT_SMARTWATCH_SYNC,
            mapOf(
                "deviceMac" to data.deviceMac,
                "steps" to data.steps,
                "heartRate" to data.heartRate,
                "batteryLevel" to data.batteryLevel,
                "timestamp" to data.timestamp
            )
        )

        return id
    }

    suspend fun deleteOldData(daysToKeep: Int = 30) {
        val beforeTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        smartwatchDataDao.deleteOldData(beforeTime)
    }

    // BLE Device Config methods
    fun getActiveDevice(): Flow<BleDeviceConfig?> {
        return bleDeviceConfigDao.getActiveDevice()
    }

    suspend fun getDeviceByMac(mac: String): BleDeviceConfig? {
        return bleDeviceConfigDao.getDeviceByMac(mac)
    }

    fun getAllDevices(): Flow<List<BleDeviceConfig>> {
        return bleDeviceConfigDao.getAllDevices()
    }

    suspend fun saveDeviceConfig(device: BleDeviceConfig): Long {
        // Deactivate all other devices first
        bleDeviceConfigDao.deactivateAllDevices()

        val id = bleDeviceConfigDao.insertDevice(device)

        // Add to ledger
        ledgerManager.addEntry(
            LedgerManager.EVENT_DEVICE_PAIRED,
            mapOf(
                "deviceMac" to device.deviceMac,
                "deviceName" to device.deviceName,
                "timestamp" to System.currentTimeMillis()
            )
        )

        return id
    }

    suspend fun updateDeviceConfig(device: BleDeviceConfig) {
        bleDeviceConfigDao.updateDevice(device)
    }

    suspend fun deleteDeviceConfig(device: BleDeviceConfig) {
        bleDeviceConfigDao.deleteDevice(device)
    }
}
