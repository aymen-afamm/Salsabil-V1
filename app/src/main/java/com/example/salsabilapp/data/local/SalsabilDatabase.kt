package com.example.salsabilapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.salsabilapp.data.local.dao.*
import com.example.salsabilapp.data.local.entity.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        UserProfile::class,
        HydrationLog::class,
        ActivityLog::class,
        SmartwatchData::class,
        LedgerEntry::class,
        ProximityEvent::class,
        BleDeviceConfig::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SalsabilDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun hydrationLogDao(): HydrationLogDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun smartwatchDataDao(): SmartwatchDataDao
    abstract fun ledgerEntryDao(): LedgerEntryDao
    abstract fun proximityEventDao(): ProximityEventDao
    abstract fun bleDeviceConfigDao(): BleDeviceConfigDao

    companion object {
        private const val DATABASE_NAME = "salsabil_database"

        @Volatile
        private var INSTANCE: SalsabilDatabase? = null

        fun getInstance(context: Context): SalsabilDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = SQLiteDatabase.getBytes("salsabil_secure_key_2024".toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalsabilDatabase::class.java,
                    DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
