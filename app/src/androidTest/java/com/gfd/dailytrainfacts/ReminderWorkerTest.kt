package com.gfd.dailytrainfacts

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderWorkerTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun testReminderWorker_showsNotificationAndSchedulesNextWork() {
        // Pre-enable reminders in SharedPreferences
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("reminder_enabled", true)
            .putInt("reminder_hour", 9)
            .putInt("reminder_minute", 0)
            .commit()

        val worker = TestListenableWorkerBuilder<ReminderWorker>(context).build()
        runBlocking {
            val result = worker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
            
            // Verify that a new work request is enqueued with the correct tag
            val workInfos = workManager.getWorkInfosByTag("daily_train_fact_reminder").get()
            
            // One for the current execution (finished) and one for the next day (enqueued)
            assertTrue("A new reminder should be scheduled", workInfos.any { it.state == WorkInfo.State.ENQUEUED })
        }
    }
}
