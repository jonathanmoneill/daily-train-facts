package com.gfd.dailytrainfacts.data

import com.gfd.dailytrainfacts.TrainFactsProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.*

class FactRepositoryTest {

    private val factDao: FactDao = mock()
    private val repository = FactRepository(factDao)

    @Test
    fun initializeDatabaseIfNeeded_syncsNewFactsAndRemovesObsolete() = runTest {
        // Mock current DB state: 1 existing fact, 1 obsolete fact
        val existingFact = Fact(text = TrainFactsProvider.facts[0])
        val obsoleteFact = Fact(text = "Old Obsolete Fact", isFavourite = false)
        val favouriteObsoleteFact = Fact(text = "Favourited Obsolete Fact", isFavourite = true)
        
        whenever(factDao.getAllFactsOnce()).thenReturn(listOf(existingFact, obsoleteFact, favouriteObsoleteFact))

        repository.initializeDatabaseIfNeeded()

        // Verify sync call
        verify(factDao).syncFacts(
            newFacts = argThat { 
                // Should contain all provider facts except the one that's already in DB
                size == TrainFactsProvider.facts.size - 1 && 
                none { it.text == existingFact.text }
            },
            obsoleteFacts = argThat {
                // Should only contain the non-favourite obsolete fact
                size == 1 && first().text == "Old Obsolete Fact"
            }
        )
    }

    @Test
    fun toggleFavourite_updatesFactCorrectly() = runTest {
        val factText = "Test Fact"
        val fact = Fact(id = 1, text = factText, isFavourite = false)
        whenever(factDao.getFactByText(factText)).thenReturn(fact)

        repository.toggleFavourite(factText)

        verify(factDao).updateFact(check {
            assertEquals(factText, it.text)
            assertEquals(true, it.isFavourite)
        })
    }

    @Test
    fun getFavouriteFacts_returnsFlowFromDao() = runTest {
        val favourites = listOf(Fact(text = "Fav"))
        val flow = flowOf(favourites)
        whenever(factDao.getFavouriteFacts()).thenReturn(flow)

        val result = repository.getFavouriteFacts().first()
        assertEquals(favourites, result)
    }

    @Test
    fun getTodayFact_returnsExistingFactIfAlreadySetForToday() = runTest {
        val today = calculateDaysSinceEpoch()
        val existingFact = Fact(text = "Existing", lastShownDate = today, isShown = true)
        whenever(factDao.getFactForToday(today)).thenReturn(existingFact)

        val result = repository.getTodayFact()

        assertEquals(existingFact, result)
        verify(factDao, never()).getRandomUnshownFact()
    }

    @Test
    fun getTodayFact_picksNewFactIfNoneSetForToday() = runTest {
        val today = calculateDaysSinceEpoch()
        val newFact = Fact(text = "New")
        whenever(factDao.getFactForToday(today)).thenReturn(null)
        whenever(factDao.getRandomUnshownFact()).thenReturn(newFact)

        val result = repository.getTodayFact()

        assertNotNull(result)
        verify(factDao).updateFact(check {
            assertEquals("New", it.text)
            assertEquals(today, it.lastShownDate)
            assertEquals(true, it.isShown)
        })
    }

    @Test
    fun getTodayFact_resetsShownStatusIfNoUnshownFactsRemain() = runTest {
        val today = calculateDaysSinceEpoch()
        val newFactAfterReset = Fact(text = "Reset Fact")
        
        whenever(factDao.getFactForToday(today)).thenReturn(null)
        whenever(factDao.getRandomUnshownFact())
            .thenReturn(null) // First call: none left
            .thenReturn(newFactAfterReset) // Second call (after reset): found one

        val result = repository.getTodayFact()

        verify(factDao).resetShownStatus()
        assertEquals("Reset Fact", result?.text)
        verify(factDao).updateFact(any())
    }

    private fun calculateDaysSinceEpoch(): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        val localTimeInMillis = now + calendar.timeZone.getOffset(now)
        return localTimeInMillis / (24 * 60 * 60 * 1000)
    }
}
