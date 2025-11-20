package com.example.salsabilapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SalsabilApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Hydration Reminder Channel
            val hydrationChannel = NotificationChannel(
                CHANNEL_HYDRATION_REMINDER,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications to remind you to drink water"
                enableVibration(true)
                setShowBadge(true)
            }

            // Foreground Service Channel
            val foregroundChannel = NotificationChannel(
                CHANNEL_FOREGROUND_SERVICE,
                "Background Services",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent notification for smartwatch and GPS tracking"
                setShowBadge(false)
            }

            // Smartwatch Sync Channel
            val smartwatchChannel = NotificationChannel(
                CHANNEL_SMARTWATCH_SYNC,
                "Smartwatch Sync",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about smartwatch connection and sync"
                enableVibration(false)
            }

            // Proximity/Kidney Support Channel
            val proximityChannel = NotificationChannel(
                CHANNEL_PROXIMITY_ALERT,
                "Kidney Support Mode",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when nearby Salsabil users are detected"
                enableVibration(true)
            }

            // Activity Tracking Channel
            val activityChannel = NotificationChannel(
                CHANNEL_ACTIVITY_TRACKING,
                "Activity Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about your activity and movement"
                enableVibration(false)
            }

            notificationManager.createNotificationChannels(
                listOf(
                    hydrationChannel,
                    foregroundChannel,
                    smartwatchChannel,
                    proximityChannel,
                    activityChannel
                )
            )
        }
    }

    companion object {
        const val CHANNEL_HYDRATION_REMINDER = "hydration_reminder_channel"
        const val CHANNEL_FOREGROUND_SERVICE = "foreground_service_channel"
        const val CHANNEL_SMARTWATCH_SYNC = "smartwatch_sync_channel"
        const val CHANNEL_PROXIMITY_ALERT = "proximity_alert_channel"
        const val CHANNEL_ACTIVITY_TRACKING = "activity_tracking_channel"
    }
}
