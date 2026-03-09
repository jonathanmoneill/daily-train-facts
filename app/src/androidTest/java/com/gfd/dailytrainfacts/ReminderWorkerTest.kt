package com.gfd.dailytrainfacts

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testReminderWorker_schedulesNextWork() {
        val worker = TestListenableWorkerBuilder<ReminderWorker>(context).build()
        runBlocking {
            val result = worker.doWork()
            assertTrue(result is androidx.work.ListenableWorker.Result.Success)
        }
    }
}
