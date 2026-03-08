package com.gfd.dailytrainfacts

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesManagerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        // Clear favorites before each test to ensure a clean state
        val prefs = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun toggleFavorite_addsFact() {
        val fact = "Trains are cool."
        
        FavoritesManager.toggleFavorite(context, fact)
        
        assertTrue("Fact should be marked as favorite", FavoritesManager.isFavorite(context, fact))
        val favorites = FavoritesManager.getFavorites(context)
        assertTrue("Favorites set should contain the fact", favorites.contains(fact))
    }

    @Test
    fun toggleFavorite_removesFact() {
        val fact = "Trains run on tracks."
        
        // Add it first
        FavoritesManager.toggleFavorite(context, fact)
        assertTrue(FavoritesManager.isFavorite(context, fact))
        
        // Toggle again to remove
        FavoritesManager.toggleFavorite(context, fact)
        
        assertFalse("Fact should no longer be a favorite", FavoritesManager.isFavorite(context, fact))
        val favorites = FavoritesManager.getFavorites(context)
        assertFalse("Favorites set should not contain the fact", favorites.contains(fact))
    }

    @Test
    fun getFavorites_returnsMultipleFacts() {
        val facts = listOf("Fact A", "Fact B", "Fact C")
        
        facts.forEach { FavoritesManager.toggleFavorite(context, it) }
        
        val savedFavorites = FavoritesManager.getFavorites(context)
        assertEquals("Should have 3 favorites saved", 3, savedFavorites.size)
        assertTrue("Should contain all added facts", savedFavorites.containsAll(facts))
    }
}
