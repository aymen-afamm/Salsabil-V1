package com.example.salsabilapp.di

import android.content.Context
import com.example.salsabilapp.data.local.SalsabilDatabase
import com.example.salsabilapp.data.local.dao.*
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SalsabilDatabase {
        return SalsabilDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: SalsabilDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideHydrationLogDao(database: SalsabilDatabase): HydrationLogDao {
        return database.hydrationLogDao()
    }

    @Provides
    @Singleton
    fun provideActivityLogDao(database: SalsabilDatabase): ActivityLogDao {
        return database.activityLogDao()
    }

    @Provides
    @Singleton
    fun provideSmartwatchDataDao(database: SalsabilDatabase): SmartwatchDataDao {
        return database.smartwatchDataDao()
    }

    @Provides
    @Singleton
    fun provideLedgerEntryDao(database: SalsabilDatabase): LedgerEntryDao {
        return database.ledgerEntryDao()
    }

    @Provides
    @Singleton
    fun provideProximityEventDao(database: SalsabilDatabase): ProximityEventDao {
        return database.proximityEventDao()
    }

    @Provides
    @Singleton
    fun provideBleDeviceConfigDao(database: SalsabilDatabase): BleDeviceConfigDao {
        return database.bleDeviceConfigDao()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}
