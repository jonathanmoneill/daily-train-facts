package com.gfd.dailytrainfacts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule WorkManager task on boot if it was enabled in preferences
            ReminderManager.rescheduleReminder(context)
        }
    }
}
