package com.gfd.dailytrainfacts

import android.content.Context
import com.gfd.dailytrainfacts.data.Fact
import com.gfd.dailytrainfacts.data.FactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class TrainFactsViewModelTest {

    private val repository: FactRepository = mock()
    private val context: Context = mock()
    private lateinit var viewModel: TrainFactsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val initialFlow = flowOf(emptyList<Fact>())
        whenever(repository.getFavoriteFacts()).thenReturn(initialFlow)
        
        viewModel = TrainFactsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun navigateTo_updatesCurrentScreen() {
        assertEquals(Screen.Home, viewModel.currentScreen.value)
        viewModel.navigateTo(Screen.Fact)
        assertEquals(Screen.Fact, viewModel.currentScreen.value)
        viewModel.navigateTo(Screen.Favorites)
        assertEquals(Screen.Favorites, viewModel.currentScreen.value)
    }

    @Test
    fun setNotificationPermissionGranted_updatesState() {
        assertEquals(false, viewModel.isNotificationPermissionGranted.value)
        viewModel.setNotificationPermissionGranted(true)
        assertEquals(true, viewModel.isNotificationPermissionGranted.value)
        viewModel.setNotificationPermissionGranted(false)
        assertEquals(false, viewModel.isNotificationPermissionGranted.value)
    }

    @Test
    fun init_loadsDataAndSubscribesToFavorites() = runTest {
        val favoriteFacts = listOf(Fact(text = "Favorite 1", isFavorite = true))
        val favoritesFlow = flowOf(favoriteFacts)
        
        whenever(repository.getFavoriteFacts()).thenReturn(favoritesFlow)
        whenever(repository.getTodayFact()).thenReturn(Fact(text = "Daily Fact"))

        viewModel.init(context)
        advanceUntilIdle()

        verify(repository).initializeDatabaseIfNeeded()
        // Check that the favorites state was actually updated by the flow
        assertEquals(favoriteFacts, viewModel.favoriteFacts.value)
        assertEquals("Daily Fact", viewModel.currentFact.value?.text)
    }

    @Test
    fun toggleFavorite_callsRepositoryAndReloadsFact() = runTest {
        val fact = Fact(id = 1, text = "Test Fact", isFavorite = false)
        val updatedFact = fact.copy(isFavorite = true)
        
        whenever(repository.getFactByText(fact.text)).thenReturn(updatedFact)

        viewModel.toggleFavorite(fact)
        advanceUntilIdle()

        verify(repository).toggleFavorite(fact.text)
        verify(repository).getFactByText(fact.text)
        assertEquals(true, viewModel.currentFact.value?.isFavorite)
    }
}
