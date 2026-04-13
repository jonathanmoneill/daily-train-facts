package com.gfd.dailytrainfacts.data

import com.gfd.dailytrainfacts.TrainFactsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class FactRepository(private val factDao: FactDao) {

    fun getFavouriteFacts(): Flow<List<Fact>> = factDao.getFavouriteFacts()

    suspend fun getFactByText(text: String): Fact? = withContext(Dispatchers.IO) {
        factDao.getFactByText(text)
    }

    suspend fun toggleFavourite(factText: String) = withContext(Dispatchers.IO) {
        val fact = factDao.getFactByText(factText)
        if (fact != null) {
            factDao.updateFact(fact.copy(isFavourite = !fact.isFavourite))
        }
    }

    suspend fun initializeDatabaseIfNeeded() {
        withContext(Dispatchers.IO) {
            val existingFacts = factDao.getAllFactsOnce()
            val existingTexts = existingFacts.map { it.text }.toSet()
            
            val providerFacts = TrainFactsProvider.facts.toSet()
            
            // Add new facts from provider
            val newFacts = providerFacts
                .filter { it !in existingTexts }
                .map { Fact(text = it) }
            
            if (newFacts.isNotEmpty()) {
                factDao.insertFacts(newFacts)
            }

            // Remove facts that are no longer in the provider,
            // but ONLY if they haven't been favourited by the user.
            val obsoleteFacts = existingFacts.filter { 
                it.text !in providerFacts && !it.isFavourite 
            }
            
            factDao.syncFacts(newFacts, obsoleteFacts)
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
