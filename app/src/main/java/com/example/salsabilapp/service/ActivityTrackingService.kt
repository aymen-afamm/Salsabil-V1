package com.example.salsabilapp.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.example.salsabilapp.data.repository.ActivityRepository
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ActivityTrackingService : Service(), SensorEventListener {

    @Inject
    lateinit var activityRepository: ActivityRepository

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var stepCount = 0
    private var initialStepCount = -1

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private var totalDistance = 0f

    override fun onCreate() {
        super.onCreate()
        setupStepCounter()
        setupLocationTracking()
    }

    private fun setupStepCounter() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            5 * 60 * 1000L // 5 minutes
        ).apply {
            setMinUpdateIntervalMillis(2 * 60 * 1000L) // 2 minutes
            setMaxUpdateDelayMillis(10 * 60 * 1000L) // 10 minutes
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    private fun handleLocationUpdate(location: Location) {
        lastLocation?.let { last ->
            val distance = last.distanceTo(location)
            totalDistance += distance
        }
        lastLocation = location
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (initialStepCount == -1) {
                initialStepCount = event.values[0].toInt()
            }
            stepCount = event.values[0].toInt() - initialStepCount
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    fun getCurrentSteps(): Int = stepCount

    fun getCurrentDistance(): Float = totalDistance

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }
}
