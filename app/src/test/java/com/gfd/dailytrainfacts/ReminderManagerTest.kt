package com.gfd.dailytrainfacts

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class ReminderManagerTest {

    @Test
    fun calculateNextOccurrence_laterToday_returnsToday() {
        val now = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 10, 0, 0) // 10:00 AM
        }.timeInMillis

        // Request reminder for 11:00 AM today
        val next = ReminderManager.calculateNextOccurrence(now, 11, 0)

        val nextCal = Calendar.getInstance().apply { timeInMillis = next }
        assertEquals(2024, nextCal.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, nextCal.get(Calendar.MONTH))
        assertEquals(1, nextCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(11, nextCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, nextCal.get(Calendar.MINUTE))
    }

    @Test
    fun calculateNextOccurrence_alreadyPassedToday_returnsTomorrow() {
        val now = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 12, 0, 0) // 12:00 PM
        }.timeInMillis

        // Request reminder for 9:00 AM (already passed today)
        val next = ReminderManager.calculateNextOccurrence(now, 9, 0)

        val nextCal = Calendar.getInstance().apply { timeInMillis = next }
        // Should be Jan 2nd
        assertEquals(2, nextCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, nextCal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun calculateNextOccurrence_exactSameTime_returnsTomorrow() {
        val now = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Request reminder for 9:00 AM (exactly now)
        val next = ReminderManager.calculateNextOccurrence(now, 9, 0)

        val nextCal = Calendar.getInstance().apply { timeInMillis = next }
        // Should be Jan 2nd to avoid double firing or infinite loops
        assertEquals(2, nextCal.get(Calendar.DAY_OF_MONTH))
    }
}
