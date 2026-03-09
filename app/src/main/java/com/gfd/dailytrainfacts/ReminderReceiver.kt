package com.gfd.dailytrainfacts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule alarm on boot if enabled
            ReminderManager.rescheduleReminder(context)
            return
        }

        // Use a coroutine scope to call the suspend function
        val app = context.applicationContext as DailyTrainFactsApplication
        val repository = app.repository
        
        CoroutineScope(Dispatchers.IO).launch {
            val fact = repository.getTodayFact()
            val factText = fact?.text ?: "Open the app to see today's train fact!"
            showNotification(context, factText)
        }
    }

    private fun showNotification(context: Context, factText: String) {
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
        
        // Schedule next day
        ReminderManager.rescheduleReminder(context)
    }
}
