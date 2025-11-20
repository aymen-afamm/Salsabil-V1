package com.example.salsabilapp.data.local.dao

import androidx.room.*
import com.example.salsabilapp.data.local.entity.BleDeviceConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface BleDeviceConfigDao {
    @Query("SELECT * FROM ble_device_config WHERE isActive = 1 LIMIT 1")
    fun getActiveDevice(): Flow<BleDeviceConfig?>

    @Query("SELECT * FROM ble_device_config WHERE deviceMac = :mac LIMIT 1")
    suspend fun getDeviceByMac(mac: String): BleDeviceConfig?

    @Query("SELECT * FROM ble_device_config ORDER BY lastConnected DESC")
    fun getAllDevices(): Flow<List<BleDeviceConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: BleDeviceConfig): Long

    @Update
    suspend fun updateDevice(device: BleDeviceConfig)

    @Query("UPDATE ble_device_config SET isActive = 0")
    suspend fun deactivateAllDevices()

    @Delete
    suspend fun deleteDevice(device: BleDeviceConfig)
}
