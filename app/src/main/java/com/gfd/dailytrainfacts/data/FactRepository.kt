package com.gfd.dailytrainfacts.data

import com.gfd.dailytrainfacts.TrainFactsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class FactRepository(private val factDao: FactDao) {

    fun getFavoriteFacts(): Flow<List<Fact>> = factDao.getFavoriteFacts()

    suspend fun getFactByText(text: String): Fact? = withContext(Dispatchers.IO) {
        factDao.getFactByText(text)
    }

    suspend fun toggleFavorite(factText: String) = withContext(Dispatchers.IO) {
        val fact = factDao.getFactByText(factText)
        if (fact != null) {
            factDao.updateFact(fact.copy(isFavorite = !fact.isFavorite))
        }
    }

    suspend fun initializeDatabaseIfNeeded() {
        withContext(Dispatchers.IO) {
            val count = factDao.getFactCount()
            if (count == 0) {
                // Initial insertion of facts from Provider
                val factsToInsert = TrainFactsProvider.facts.map { Fact(text = it) }
                factDao.insertFacts(factsToInsert)
            }
        }
    }

    suspend fun getTodayFact(): Fact? = withContext(Dispatchers.IO) {
        val today = calculateDaysSinceEpoch()
        
        // Check if we already picked one for today
        val existingFact = factDao.getFactForToday(today)
        if (existingFact != null) return@withContext existingFact

        // Otherwise, pick a new one
        var nextFact = factDao.getRandomUnshownFact()

        // If no unshown facts remain, reset the cycle and pick again
        if (nextFact == null) {
            factDao.resetShownStatus()
            nextFact = factDao.getRandomUnshownFact()
        }

        // Mark it as shown and assign it to today
        nextFact?.let {
            val updatedFact = it.copy(isShown = true, lastShownDate = today)
            factDao.updateFact(updatedFact)
            return@withContext updatedFact
        }
        
        return@withContext null
    }

    private fun calculateDaysSinceEpoch(): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        val localTimeInMillis = now + calendar.timeZone.getOffset(now)
        return localTimeInMillis / (24 * 60 * 60 * 1000)
    }
}
