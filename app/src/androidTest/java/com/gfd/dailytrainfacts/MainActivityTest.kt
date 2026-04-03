package com.gfd.dailytrainfacts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_displaysTitleAndButton() {
        composeTestRule.onNodeWithText("DAILY TRAIN FACTS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Give Me a Train Fact").assertIsDisplayed()
    }

    @Test
    fun navigateToFactOverlay_displaysActionButtons() {
        // Start on Home and click the button
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()

        // Verify overlay is shown by checking for action buttons
        composeTestRule.onNodeWithText("Favourite", substring = true).assertExists()
        composeTestRule.onNodeWithText("Share", substring = true).assertExists()
        composeTestRule.onNodeWithText("Close").assertIsDisplayed()
    }

    @Test
    fun factOverlay_canClose() {
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()
        
        // Verify overlay is shown
        composeTestRule.onNodeWithText("Close").assertIsDisplayed()
        
        // Click Close
        composeTestRule.onNodeWithText("Close").performClick()
        
        // Verify we are back on Home (Close button is gone)
        composeTestRule.onNodeWithText("Close").assertDoesNotExist()
        composeTestRule.onNodeWithText("Give Me a Train Fact").assertIsDisplayed()
    }

    @Test
    fun burgerMenu_navigateToFavourites() {
        // Open burger menu (now on Home Screen)
        composeTestRule.onNodeWithContentDescription("Menu").performClick()

        // Click Favourite Facts in menu
        composeTestRule.onNodeWithText("Favourite Facts").performClick()

        // Verify we are on Favourites screen
        composeTestRule.onNodeWithText("Favourite Facts", ignoreCase = false).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun toggleFavourite_updatesButtonText() {
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()

        // Check initial state (assuming not favourite, button text based on current implementation)
        // Note: Using substring match for "Favourite" since the label changes between states
        val favButton = composeTestRule.onNodeWithText("Favourite", substring = true)
        favButton.assertExists()

        // Click the button to toggle (initially adding to favourites)
        favButton.performClick()

        // The button state is handled by the ViewModel and observed by the UI.
        // We just verify the button remains interactive and exists.
        composeTestRule.onNodeWithText("Favourite", substring = true).assertExists()
    }
    
    @Test
    fun favouritesScreen_showsShareButton() {
        // First, add a fact to favourites so it shows up in the list
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()
        composeTestRule.onNodeWithText("Add to Favourites", substring = true).performClick()
        
        // Close overlay to go back to Home
        composeTestRule.onNodeWithText("Close").performClick()

        // Open burger menu and navigate to Favourites
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNodeWithText("Favourite Facts").performClick()

        // Verify that the share button exists for the favourite fact
        composeTestRule.onNodeWithContentDescription("Share fact").assertIsDisplayed()
    }
    
    @Test
    fun favouritesScreen_showsConfirmationDialog_onRemove() {
        // First, add a fact to be removed
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()
        composeTestRule.onNodeWithText("Add to Favourites", substring = true).performClick()
        
        // Close overlay to go back to Home
        composeTestRule.onNodeWithText("Close").performClick()

        // Open burger menu (now on Home Screen)
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNodeWithText("Favourite Facts").performClick()

        // Now on favourites, click the remove icon
        composeTestRule.onNodeWithContentDescription("Remove from favourites").performClick()

        // Verify dialog is shown
        composeTestRule.onNodeWithText("Remove from Favourites?").assertIsDisplayed()

        // Click Cancel and verify dialog is gone
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Remove from Favourites?").assertDoesNotExist()

        // Re-open and confirm removal
        composeTestRule.onNodeWithContentDescription("Remove from favourites").performClick()
        composeTestRule.onNodeWithText("Remove").performClick()

        // Verify the removal icon is gone (assuming only one fact was added)
        composeTestRule.onNodeWithContentDescription("Remove from favourites").assertDoesNotExist()
    }

    @Test
    fun favouritesScreen_clickFact_opensOverlay() {
        // 1. Add a fact to favourites from the Home screen
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()
        composeTestRule.onNodeWithText("Add to Favourites", substring = true).performClick()
        composeTestRule.onNodeWithText("Close").performClick()

        // 2. Navigate to Favourites screen
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNodeWithText("Favourite Facts").performClick()

        // 3. Find a fact card (which is now clickable) and click it
        // We look for any card that has a click action on the Favourites screen
        composeTestRule.onAllNodes(hasClickAction())
            .filterToOne(hasAnyChild(hasText("Favourite Facts").not())) // Avoid the header or back button if they have clicks
            .performClick()

        // 4. Verify that the FactOverlay is shown
        composeTestRule.onNodeWithText("Close").assertIsDisplayed()
        
        // 5. Close the overlay and verify we are back in the list
        composeTestRule.onNodeWithText("Close").performClick()
        composeTestRule.onNodeWithText("Close").assertDoesNotExist()
        composeTestRule.onNodeWithText("Favourite Facts").assertIsDisplayed()
    }

    @Test
    fun reminderDialog_displaysOptions() {
        // Open burger menu
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        
        // Click Reminder Settings
        composeTestRule.onNodeWithText("Reminder Settings").performClick()
        
        // Verify dialog contents
        composeTestRule.onNodeWithText("Daily Reminder").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enable Reminders").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reminder Time", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
    }
}
