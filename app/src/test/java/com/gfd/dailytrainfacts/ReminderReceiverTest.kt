package com.gfd.dailytrainfacts

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ReminderReceiverTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun onReceive_bootCompleted_triggersWorkManager() {
        // Pre-enable reminders in SharedPreferences
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("reminder_enabled", true)
            .putInt("reminder_hour", 9)
            .putInt("reminder_minute", 0)
            .commit()

        val receiver = ReminderReceiver()
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(context, intent)
        
        // Verify that WorkManager has enqueued the work request
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag("daily_train_fact_reminder").get()
        
        assertTrue("Work should be enqueued after boot if reminders are enabled", 
            workInfos.any { it.state == WorkInfo.State.ENQUEUED })
    }
}
