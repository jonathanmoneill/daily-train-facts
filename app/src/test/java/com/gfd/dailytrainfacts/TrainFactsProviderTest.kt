package com.gfd.dailytrainfacts

import org.junit.Assert.*
import org.junit.Test

class TrainFactsProviderTest {

    @Test
    fun getFactForToday_returnsNonEmptyString() {
        val fact = TrainFactsProvider.getFactForToday()
        assertNotNull(fact)
        assertTrue(fact.isNotEmpty())
    }

    @Test
    fun getFactForTime_isConsistentForSameDay() {
        val time = 1704067200000L // 2024-01-01 00:00:00 UTC
        val fact1 = TrainFactsProvider.getFactForTime(time)
        val fact2 = TrainFactsProvider.getFactForTime(time + 1000) // 1 second later
        val fact3 = TrainFactsProvider.getFactForTime(time + 86300000) // End of same day
        
        assertEquals(fact1, fact2)
        assertEquals(fact1, fact3)
    }

    @Test
    fun getFactForTime_changesNextDay() {
        val time = 1704067200000L // 2024-01-01 00:00:00 UTC
        val fact1 = TrainFactsProvider.getFactForTime(time)
        val fact2 = TrainFactsProvider.getFactForTime(time + 86400000) // Next day
        
        assertNotEquals(fact1, fact2)
    }

    @Test
    fun facts_list_hasAtLeast366Items() {
        val count = TrainFactsProvider.facts.size
        assertTrue("Expected at least 366 facts, but found $count", count >= 366)
    }

    @Test
    fun facts_rotation_isStable() {
        val time = 1704067200000L // Start time
        val totalFacts = TrainFactsProvider.facts.size
        
        // Check uniqueness over the rotation period
        val seenFacts = mutableSetOf<String>()
        for (i in 0 until totalFacts) {
            val fact = TrainFactsProvider.getFactForTime(time + (i.toLong() * 24 * 60 * 60 * 1000))
            assertTrue("Fact repeated within rotation period: $fact", seenFacts.add(fact))
        }
        
        assertEquals(totalFacts, seenFacts.size)
    }
}
