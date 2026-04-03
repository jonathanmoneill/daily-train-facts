package com.gfd.dailytrainfacts

import android.content.Context
import android.content.SharedPreferences
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.gfd.dailytrainfacts.data.Fact
import com.gfd.dailytrainfacts.data.FactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class TrainFactsViewModelTest {

    private val repository: FactRepository = mock()
    private val context: Context = mock()
    private val sharedPrefs: SharedPreferences = mock()
    private val sharedPrefsEditor: SharedPreferences.Editor = mock()
    private val workManager: WorkManager = mock()
    private lateinit var viewModel: TrainFactsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock SharedPreferences to prevent NPE during init
        whenever(context.getSharedPreferences(any<String>(), any<Int>())).thenReturn(sharedPrefs)
        whenever(sharedPrefs.edit()).thenReturn(sharedPrefsEditor)
        whenever(sharedPrefsEditor.putBoolean(any<String>(), any<Boolean>())).thenReturn(sharedPrefsEditor)
        whenever(sharedPrefsEditor.putInt(any<String>(), any<Int>())).thenReturn(sharedPrefsEditor)
        whenever(sharedPrefsEditor.putString(any<String>(), anyOrNull<String>())).thenReturn(sharedPrefsEditor)

        whenever(sharedPrefs.getBoolean(any<String>(), any<Boolean>())).thenReturn(false)
        whenever(sharedPrefs.getInt(any<String>(), any<Int>())).thenReturn(0)

        val initialFlow = flowOf(emptyList<Fact>())
        whenever(repository.getFavouriteFacts()).thenReturn(initialFlow)
        
        viewModel = TrainFactsViewModel(repository, workManager)
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

    @Test
    fun toggleFavourite_updatesSelectedFavouriteFact() = runTest {
        val fact = Fact(id = 1, text = "Test Fact", isFavourite = true)
        val updatedFact = fact.copy(isFavourite = false)
        
        whenever(repository.getFactByText(fact.text)).thenReturn(updatedFact)
        
        viewModel.selectFavouriteFact(fact)
        assertEquals(fact, viewModel.selectedFavouriteFact.value)

        viewModel.toggleFavourite(fact)
        advanceUntilIdle()

        assertEquals(updatedFact, viewModel.selectedFavouriteFact.value)
    }

    @Test
    fun selectFavouriteFact_updatesState() {
        val fact = Fact(text = "Test Fact")
        assertNull(viewModel.selectedFavouriteFact.value)
        
        viewModel.selectFavouriteFact(fact)
        assertEquals(fact, viewModel.selectedFavouriteFact.value)
        
        viewModel.selectFavouriteFact(null)
        assertNull(viewModel.selectedFavouriteFact.value)
    }

    @Test
    fun toggleReminder_updatesState() {
        viewModel.toggleReminder(context, true)
        assertEquals(true, viewModel.isReminderEnabled.value)
        verify(workManager).enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>())
        
        viewModel.toggleReminder(context, false)
        assertEquals(false, viewModel.isReminderEnabled.value)
        verify(workManager).cancelAllWorkByTag(any<String>())
    }

    @Test
    fun updateReminderTime_updatesStateAndSyncsIfEnabled() {
        viewModel.toggleReminder(context, true)
        viewModel.updateReminderTime(context, 10, 30)
        
        assertEquals(Pair(10, 30), viewModel.reminderTime.value)
        verify(workManager, times(2)).enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>())
    }
}
