package com.gfd.dailytrainfacts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        showNotification(applicationContext)
        
        // Manual reschedule for next day to ensure logic stays consistent with ReminderManager
        ReminderManager.rescheduleReminder(applicationContext)
        
        return Result.success()
    }

    private suspend fun showNotification(context: Context) {
        val channelId = "daily_train_fact_reminder"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Train Fact Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Get fact from Room for consistency
        val app = context.applicationContext as DailyTrainFactsApplication
        val repository = app.repository
        val count = repository.getFactCount()
        
        val factText = if (count > 0) {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = now
            val localTimeInMillis = now + calendar.timeZone.getOffset(now)
            val daysSinceEpoch = localTimeInMillis / (24 * 60 * 60 * 1000)
            val index = (daysSinceEpoch % count).toInt()
            repository.getFactAtIndex(index)?.text ?: TrainFactsProvider.getFactForToday()
        } else {
            TrainFactsProvider.getFactForToday()
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Your Daily Train Fact")
            .setContentText(factText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(factText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
