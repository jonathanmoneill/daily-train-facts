package com.gfd.dailytrainfacts.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FactDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var factDao: FactDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        factDao = database.factDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetAllFacts() = runBlocking {
        val facts = listOf(
            Fact(text = "Fact 1"),
            Fact(text = "Fact 2")
        )
        factDao.insertFacts(facts)
        val allFacts = factDao.getAllFacts().first()
        assertEquals(2, allFacts.size)
        assertEquals("Fact 1", allFacts[0].text)
    }

    @Test
    fun toggleFavouriteAndGetFavourites() = runBlocking {
        val fact = Fact(text = "Favourite Fact", isFavourite = false)
        factDao.insertFacts(listOf(fact))
        
        val insertedFact = factDao.getFactByText("Favourite Fact")!!
        factDao.updateFact(insertedFact.copy(isFavourite = true))
        
        val favourites = factDao.getFavouriteFacts().first()
        assertEquals(1, favourites.size)
        assertTrue(favourites[0].isFavourite)
    }
}
