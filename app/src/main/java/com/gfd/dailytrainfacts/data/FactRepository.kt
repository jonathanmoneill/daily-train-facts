package com.gfd.dailytrainfacts.data

import com.gfd.dailytrainfacts.TrainFactsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FactRepository(private val factDao: FactDao) {

    fun getFavoriteFacts(): Flow<List<Fact>> = factDao.getFavoriteFacts()

    suspend fun getFactCount(): Int = factDao.getFactCount()

    suspend fun getFactAtIndex(index: Int): Fact? = withContext(Dispatchers.IO) {
        factDao.getFactAtIndex(index)
    }

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
                // Use the shuffled list from Provider to maintain rotation consistency
                val factsToInsert = TrainFactsProvider.facts.map { Fact(text = it) }
                factDao.insertFacts(factsToInsert)
            }
        }
    }
}
