package com.gfd.dailytrainfacts

import android.content.Context
import android.content.SharedPreferences
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
    private val sharedPrefs: SharedPreferences = mock()
    private lateinit var viewModel: TrainFactsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock SharedPreferences to prevent NPE during init
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPrefs)
        whenever(sharedPrefs.getBoolean(any(), any())).thenReturn(false)
        whenever(sharedPrefs.getInt(any(), any())).thenReturn(0)

        val initialFlow = flowOf(emptyList<Fact>())
        whenever(repository.getFavouriteFacts()).thenReturn(initialFlow)
        
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
        viewModel.navigateTo(Screen.Favourites)
        assertEquals(Screen.Favourites, viewModel.currentScreen.value)
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
    fun init_loadsDataAndSubscribesToFavourites() = runTest {
        val favouriteFacts = listOf(Fact(text = "Favourite 1", isFavourite = true))
        val favouritesFlow = flowOf(favouriteFacts)
        
        whenever(repository.getFavouriteFacts()).thenReturn(favouritesFlow)
        whenever(repository.getTodayFact()).thenReturn(Fact(text = "Daily Fact"))

        viewModel.init(context)
        advanceUntilIdle()

        verify(repository).initializeDatabaseIfNeeded()
        // Check that the favourites state was actually updated by the flow
        assertEquals(favouriteFacts, viewModel.favouriteFacts.value)
        assertEquals("Daily Fact", viewModel.currentFact.value?.text)
    }

    @Test
    fun toggleFavourite_callsRepositoryAndReloadsFact() = runTest {
        val fact = Fact(id = 1, text = "Test Fact", isFavourite = false)
        val updatedFact = fact.copy(isFavourite = true)
        
        whenever(repository.getFactByText(fact.text)).thenReturn(updatedFact)

        viewModel.toggleFavourite(fact)
        advanceUntilIdle()

        verify(repository).toggleFavourite(fact.text)
        verify(repository).getFactByText(fact.text)
        assertEquals(true, viewModel.currentFact.value?.isFavourite)
    }
}
