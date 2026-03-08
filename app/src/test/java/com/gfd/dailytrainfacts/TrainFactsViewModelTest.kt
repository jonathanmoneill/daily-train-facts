package com.gfd.dailytrainfacts

import org.junit.Assert.assertEquals
import org.junit.Test

class TrainFactsViewModelTest {

    @Test
    fun navigateTo_updatesCurrentScreen() {
        val viewModel = TrainFactsViewModel()
        // Initial state should be Home
        assertEquals(Screen.Home, viewModel.currentScreen.value)

        // Navigate to Fact screen
        viewModel.navigateTo(Screen.Fact)
        assertEquals(Screen.Fact, viewModel.currentScreen.value)

        // Navigate to Favorites screen
        viewModel.navigateTo(Screen.Favorites)
        assertEquals(Screen.Favorites, viewModel.currentScreen.value)
    }

    @Test
    fun setNotificationPermissionGranted_updatesState() {
        val viewModel = TrainFactsViewModel()
        // Initial state should be false
        assertEquals(false, viewModel.isNotificationPermissionGranted.value)

        // Grant permission
        viewModel.setNotificationPermissionGranted(true)
        assertEquals(true, viewModel.isNotificationPermissionGranted.value)

        // Revoke permission
        viewModel.setNotificationPermissionGranted(false)
        assertEquals(false, viewModel.isNotificationPermissionGranted.value)
    }
}
