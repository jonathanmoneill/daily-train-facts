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
        composeTestRule.onNodeWithText("Add", substring = true).assertExists()
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

        // Check initial state (assuming not favourite, button says "Add")
        val addButton = composeTestRule.onNodeWithText("Add", substring = true)
        addButton.assertExists()

        // Click Add to Favourites
        addButton.performClick()

        // Button should now say "Favourite"
        composeTestRule.onNodeWithText("Favourite", substring = true).assertExists()

        // Click again to remove
        composeTestRule.onNodeWithText("Favourite", substring = true).performClick()

        // Should be back to Add
        composeTestRule.onNodeWithText("Add", substring = true).assertExists()
    }
    
    @Test
    fun favouritesScreen_showsConfirmationDialog_onRemove() {
        // First, add a fact to be removed
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()
        composeTestRule.onNodeWithText("Add", substring = true).performClick()
        
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
}
