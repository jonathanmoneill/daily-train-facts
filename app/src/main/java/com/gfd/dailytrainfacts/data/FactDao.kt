package com.gfd.dailytrainfacts.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FactDao {
    @Query("SELECT * FROM facts")
    suspend fun getAllFactsOnce(): List<Fact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacts(facts: List<Fact>)

    @Query("SELECT COUNT(*) FROM facts")
    suspend fun getFactCount(): Int

    @Query("SELECT * FROM facts WHERE text = :text LIMIT 1")
    suspend fun getFactByText(text: String): Fact?

    @Update
    suspend fun updateFact(fact: Fact)

    @Delete
    suspend fun deleteFacts(facts: List<Fact>)

    @Query("SELECT * FROM facts WHERE isFavourite = 1")
    fun getFavouriteFacts(): Flow<List<Fact>>

    @Query("SELECT * FROM facts WHERE lastShownDate = :today LIMIT 1")
    suspend fun getFactForToday(today: Long): Fact?

    @Query("SELECT * FROM facts WHERE isShown = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomUnshownFact(): Fact?

    @Query("UPDATE facts SET isShown = 0")
    suspend fun resetShownStatus()

    @Transaction
    suspend fun syncFacts(
        newFacts: List<Fact>,
        obsoleteFacts: List<Fact>
    ) {
        if (newFacts.isNotEmpty()) {
            insertFacts(newFacts)
        }
        if (obsoleteFacts.isNotEmpty()) {
            deleteFacts(obsoleteFacts)
        }
    }
}
