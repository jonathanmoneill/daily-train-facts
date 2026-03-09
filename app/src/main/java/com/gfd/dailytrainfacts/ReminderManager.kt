package com.gfd.dailytrainfacts

import android.content.Context
import androidx.core.content.edit
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderManager {
    private const val PREFS_NAME = "reminder_prefs"
    private const val KEY_ENABLED = "reminder_enabled"
    private const val KEY_HOUR = "reminder_hour"
    private const val KEY_MINUTE = "reminder_minute"
    private const val WORK_TAG = "daily_train_fact_reminder"

    fun setReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_ENABLED, enabled)
            putInt(KEY_HOUR, hour)
            putInt(KEY_MINUTE, minute)
        }

        if (enabled) {
            scheduleWork(context, hour, minute)
        } else {
            cancelWork(context)
        }
    }

    fun isReminderEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
    }

    fun getReminderTime(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Pair(prefs.getInt(KEY_HOUR, 9), prefs.getInt(KEY_MINUTE, 0))
    }

    fun rescheduleReminder(context: Context) {
        if (isReminderEnabled(context)) {
            val (hour, minute) = getReminderTime(context)
            scheduleWork(context, hour, minute)
        }
    }

    private fun scheduleWork(context: Context, hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(context)
        
        val initialDelay = calculateNextOccurrence(System.currentTimeMillis(), hour, minute) - System.currentTimeMillis()
        
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(
            WORK_TAG,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    internal fun calculateNextOccurrence(now: Long, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    private fun cancelWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
}
