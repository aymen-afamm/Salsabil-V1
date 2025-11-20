package com.example.salsabilapp.domain

import com.example.salsabilapp.data.local.entity.UserProfile
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class HydrationEngine @Inject constructor() {

    /**
     * Calculate recommended daily hydration goal based on user profile and activity
     * Formula: base = weightKg * 35
     *          activityExtra = (steps / 1000) * 150
     *          gpsExtra = (distanceKm * 300)
     *          finalGoal = clamp(base + activityExtra + gpsExtra, 1000, 6000)
     */
    fun calculateDailyGoal(
        userProfile: UserProfile,
        dailySteps: Int = 0,
        distanceKm: Float = 0f
    ): Int {
        // If user has overridden the goal, return their custom goal
        if (userProfile.isGoalOverridden) {
            return userProfile.dailyGoalMl
        }

        // Base hydration based on weight
        val baseHydration = (userProfile.weightKg * 35).toInt()

        // Additional hydration based on steps
        val activityExtra = (dailySteps / 1000) * 150

        // Additional hydration based on GPS distance
        val gpsExtra = (distanceKm * 300).toInt()

        // Calculate total
        var totalGoal = baseHydration + activityExtra + gpsExtra

        // Adjust for kidney condition (increase by 20%)
        if (userProfile.hasKidneyCondition) {
            totalGoal = (totalGoal * 1.2).toInt()
        }

        // Clamp between 1000ml and 6000ml
        return clamp(totalGoal, 1000, 6000)
    }

    /**
     * Calculate hydration goal based on heart rate during activity
     */
    fun adjustGoalForHeartRate(baseGoal: Int, averageHeartRate: Int, age: Int): Int {
        // Calculate max heart rate (220 - age)
        val maxHeartRate = 220 - age

        // Calculate percentage of max heart rate
        val hrPercentage = (averageHeartRate.toFloat() / maxHeartRate) * 100

        // Adjust goal based on heart rate intensity
        val adjustment = when {
            hrPercentage > 85 -> 1.3f // Very high intensity
            hrPercentage > 70 -> 1.2f // High intensity
            hrPercentage > 50 -> 1.1f // Moderate intensity
            else -> 1.0f // Low intensity
        }

        return (baseGoal * adjustment).toInt()
    }

    /**
     * Calculate hydration reminder interval based on current progress
     */
    fun calculateReminderInterval(
        currentIntake: Int,
        dailyGoal: Int,
        hoursRemaining: Int
    ): Long {
        val remaining = max(0, dailyGoal - currentIntake)
        val progress = (currentIntake.toFloat() / dailyGoal) * 100

        // More frequent reminders if behind schedule
        val intervalMinutes = when {
            progress >= 90 -> 120 // Every 2 hours if almost done
            progress >= 70 -> 90  // Every 1.5 hours if on track
            progress >= 50 -> 60  // Every hour if slightly behind
            progress >= 30 -> 45  // Every 45 minutes if behind
            else -> 30            // Every 30 minutes if very behind
        }

        return intervalMinutes * 60 * 1000L // Convert to milliseconds
    }

    /**
     * Determine if user should be reminded based on distance walked
     */
    fun shouldRemindBasedOnDistance(distanceMeters: Float, lastHydrationTime: Long): Boolean {
        val timeSinceLastHydration = System.currentTimeMillis() - lastHydrationTime
        val minutesSinceLastHydration = timeSinceLastHydration / (60 * 1000)

        // Remind if walked more than 2km without hydrating in last 30 minutes
        return distanceMeters > 2000 && minutesSinceLastHydration > 30
    }

    /**
     * Get hydration status message
     */
    fun getHydrationStatus(currentIntake: Int, dailyGoal: Int): HydrationStatus {
        val percentage = (currentIntake.toFloat() / dailyGoal) * 100

        return when {
            percentage >= 100 -> HydrationStatus.EXCELLENT
            percentage >= 80 -> HydrationStatus.GOOD
            percentage >= 60 -> HydrationStatus.FAIR
            percentage >= 40 -> HydrationStatus.BEHIND
            else -> HydrationStatus.CRITICAL
        }
    }

    private fun clamp(value: Int, min: Int, max: Int): Int {
        return max(min, min(max, value))
    }

    enum class HydrationStatus {
        EXCELLENT,
        GOOD,
        FAIR,
        BEHIND,
        CRITICAL
    }
}
