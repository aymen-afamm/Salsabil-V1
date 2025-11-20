package com.example.salsabilapp.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.salsabilapp.MainActivity
import com.example.salsabilapp.R
import com.example.salsabilapp.SalsabilApplication
import com.example.salsabilapp.data.repository.ProximityRepository
import com.example.salsabilapp.data.repository.SmartwatchRepository
import com.example.salsabilapp.domain.ble.BleManager
import com.example.salsabilapp.domain.ble.ProximityScanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class SalsabilForegroundService : Service() {

    @Inject
    lateinit var bleManager: BleManager

    @Inject
    lateinit var proximityScanner: ProximityScanner

    @Inject
    lateinit var smartwatchRepository: SmartwatchRepository

    @Inject
    lateinit var proximityRepository: ProximityRepository

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var isProximityModeEnabled = false
    private var isSmartwatchConnected = false

    override fun onCreate() {
        super.onCreate()
        startForeground()
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_PROXIMITY_MODE -> {
                isProximityModeEnabled = true
                startProximityMode()
            }
            ACTION_STOP_PROXIMITY_MODE -> {
                isProximityModeEnabled = false
                stopProximityMode()
            }
            ACTION_CONNECT_SMARTWATCH -> {
                val deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS)
                deviceAddress?.let { connectToSmartwatch(it) }
            }
            ACTION_DISCONNECT_SMARTWATCH -> {
                disconnectSmartwatch()
            }
        }

        return START_STICKY
    }

    private fun startForeground() {
        val notification = createNotification(
            "Salsabil is running",
            "Monitoring your hydration and activity"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startMonitoring() {
        // Monitor BLE connection state
        serviceScope.launch {
            bleManager.connectionState.collectLatest { state ->
                isSmartwatchConnected = state == BleManager.ConnectionState.READY
                updateNotification()
            }
        }

        // Monitor proximity events
        serviceScope.launch {
            proximityScanner.nearbyUsers.collectLatest { nearbyUsers ->
                if (nearbyUsers.isNotEmpty() && isProximityModeEnabled) {
                    handleProximityDetection(nearbyUsers)
                }
            }
        }

        // Periodic UUID rotation for privacy (every 15 minutes)
        serviceScope.launch {
            while (isActive) {
                delay(15 * 60 * 1000L) // 15 minutes
                if (isProximityModeEnabled) {
                    proximityScanner.rotateUuid()
                }
            }
        }

        // Cleanup old proximity users (every minute)
        serviceScope.launch {
            while (isActive) {
                delay(60 * 1000L) // 1 minute
                proximityScanner.cleanupOldUsers()
            }
        }
    }

    private fun startProximityMode() {
        proximityScanner.startAdvertising()
        proximityScanner.startScanning()
        updateNotification("Kidney Support Mode active")
    }

    private fun stopProximityMode() {
        proximityScanner.stopAdvertising()
        proximityScanner.stopScanning()
        updateNotification()
    }

    private fun connectToSmartwatch(deviceAddress: String) {
        bleManager.connectToDevice(deviceAddress)
        updateNotification("Connecting to smartwatch...")
    }

    private fun disconnectSmartwatch() {
        bleManager.disconnect()
        updateNotification()
    }

    private suspend fun handleProximityDetection(nearbyUsers: List<ProximityScanner.NearbyUser>) {
        for (user in nearbyUsers) {
            // Only notify for nearby users (not far away)
            if (user.distance == "immediate" || user.distance == "near") {
                // Save to database
                proximityRepository.addProximityEvent(
                    ephemeralUuid = user.ephemeralUuid,
                    rssi = user.rssi,
                    distance = user.distance,
                    actionTaken = "notified"
                )

                // Send notification
                sendProximityNotification()
            }
        }
    }

    private fun sendProximityNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, SalsabilApplication.CHANNEL_PROXIMITY_ALERT)
            .setContentTitle("Nearby Salsabil User Detected")
            .setContentText("A fellow Salsabil user is nearby. Do you need help?")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(PROXIMITY_NOTIFICATION_ID, notification)
    }

    private fun updateNotification(customMessage: String? = null) {
        val message = customMessage ?: buildStatusMessage()
        val notification = createNotification("Salsabil is running", message)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildStatusMessage(): String {
        val parts = mutableListOf<String>()

        if (isSmartwatchConnected) {
            parts.add("Smartwatch connected")
        }

        if (isProximityModeEnabled) {
            parts.add("Kidney Support Mode active")
        }

        return if (parts.isEmpty()) {
            "Monitoring your hydration and activity"
        } else {
            parts.joinToString(" • ")
        }
    }

    private fun createNotification(title: String, message: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SalsabilApplication.CHANNEL_FOREGROUND_SERVICE)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        bleManager.disconnect()
        proximityScanner.stopAdvertising()
        proximityScanner.stopScanning()
    }

    inner class LocalBinder : Binder() {
        fun getService(): SalsabilForegroundService = this@SalsabilForegroundService
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val PROXIMITY_NOTIFICATION_ID = 1002

        const val ACTION_START_PROXIMITY_MODE = "com.example.salsabilapp.START_PROXIMITY_MODE"
        const val ACTION_STOP_PROXIMITY_MODE = "com.example.salsabilapp.STOP_PROXIMITY_MODE"
        const val ACTION_CONNECT_SMARTWATCH = "com.example.salsabilapp.CONNECT_SMARTWATCH"
        const val ACTION_DISCONNECT_SMARTWATCH = "com.example.salsabilapp.DISCONNECT_SMARTWATCH"
        const val EXTRA_DEVICE_ADDRESS = "device_address"
    }
}
