package com.example.salsabilapp.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.salsabilapp.MainActivity
import com.example.salsabilapp.R
import com.example.salsabilapp.SalsabilApplication
import com.example.salsabilapp.data.local.LedgerManager
import com.example.salsabilapp.data.repository.HydrationRepository
import com.example.salsabilapp.data.repository.UserRepository
import com.example.salsabilapp.domain.HydrationEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class HydrationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val hydrationRepository: HydrationRepository,
    private val userRepository: UserRepository,
    private val hydrationEngine: HydrationEngine,
    private val ledgerManager: LedgerManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date())

            // Get current hydration intake
            val currentIntake = hydrationRepository.getTotalForDate(today).first() ?: 0

            // Get user profile and daily goal
            val userProfile = userRepository.getUserProfileOnce()
            val dailyGoal = userProfile?.dailyGoalMl ?: 2000

            // Calculate progress
            val progress = (currentIntake.toFloat() / dailyGoal) * 100

            // Send reminder if not at goal
            if (progress < 100) {
                sendReminderNotification(currentIntake, dailyGoal)

                // Log to ledger
                ledgerManager.addEntry(
                    LedgerManager.EVENT_REMINDER_SENT,
                    mapOf(
                        "currentIntake" to currentIntake,
                        "dailyGoal" to dailyGoal,
                        "progress" to progress
                    )
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendReminderNotification(currentIntake: Int, dailyGoal: Int) {
        val remaining = dailyGoal - currentIntake
        val remainingLiters = remaining / 1000f

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            SalsabilApplication.CHANNEL_HYDRATION_REMINDER
        )
            .setContentTitle("Time to Hydrate! 💧")
            .setContentText("You still need ${remainingLiters}L to reach your daily goal")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 2001
        const val WORK_NAME = "hydration_reminder_work"
    }
}
