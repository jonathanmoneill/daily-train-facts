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
    fun initializeDatabaseIfNeeded_insertsFactsWhenEmpty() = runTest {
        whenever(factDao.getFactCount()).thenReturn(0)

        repository.initializeDatabaseIfNeeded()

        verify(factDao).getFactCount()
        verify(factDao).insertFacts(argThat { size == TrainFactsProvider.facts.size })
    }

    @Test
    fun initializeDatabaseIfNeeded_doesNotInsertWhenNotEmpty() = runTest {
        whenever(factDao.getFactCount()).thenReturn(100)

        repository.initializeDatabaseIfNeeded()

        verify(factDao).getFactCount()
        verify(factDao, never()).insertFacts(any())
    }

    @Test
    fun toggleFavorite_updatesFactCorrectly() = runTest {
        val factText = "Test Fact"
        val fact = Fact(id = 1, text = factText, isFavorite = false)
        whenever(factDao.getFactByText(factText)).thenReturn(fact)

        repository.toggleFavorite(factText)

        verify(factDao).updateFact(check {
            assertEquals(factText, it.text)
            assertEquals(true, it.isFavorite)
        })
    }

    @Test
    fun getFavoriteFacts_returnsFlowFromDao() = runTest {
        val favorites = listOf(Fact(text = "Fav"))
        val flow = flowOf(favorites)
        val stub = doReturn(flow).whenever(factDao).getFavoriteFacts()
        assertNotNull(stub)

        val result = repository.getFavoriteFacts().first()
        val verification = verify(factDao).getFavoriteFacts()
        assertNotNull(verification)
        assertEquals(favorites, result)
    }
}
