package com.gfd.dailytrainfacts.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FactDao {
    @Query("SELECT * FROM facts")
    fun getAllFacts(): Flow<List<Fact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacts(facts: List<Fact>)

    @Query("SELECT COUNT(*) FROM facts")
    suspend fun getFactCount(): Int

    @Query("SELECT * FROM facts LIMIT 1 OFFSET :index")
    suspend fun getFactAtIndex(index: Int): Fact?

    @Query("SELECT * FROM facts WHERE text = :text LIMIT 1")
    suspend fun getFactByText(text: String): Fact?

    @Update
    suspend fun updateFact(fact: Fact)

    @Query("SELECT * FROM facts WHERE isFavorite = 1")
    fun getFavoriteFacts(): Flow<List<Fact>>
}
