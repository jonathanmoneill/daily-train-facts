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
    fun navigateToFactScreen_displaysActionButtons() {
        // Start on Home and click the button
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()

        // Verify we are on Fact screen by checking for action buttons
        composeTestRule.onNodeWithText("Add to Favorites", substring = true).assertExists()
        composeTestRule.onNodeWithText("Share Fact", substring = true).assertExists()
    }

    @Test
    fun factScreen_canScrollToExitButton() {
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()

        // Find the Exit button. If it's not visible initially (long text), perform scroll.
        // The content is wrapped in a Column with verticalScroll(scrollState)
        composeTestRule.onNodeWithText("Exit").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun burgerMenu_navigateToFavorites() {
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()

        // Open burger menu
        composeTestRule.onNodeWithContentDescription("Menu").performClick()

        // Click Favorite Facts in menu
        composeTestRule.onNodeWithText("Favorite Facts").performClick()

        // Verify we are on Favorites screen
        composeTestRule.onNodeWithText("Favorite Facts", ignoreCase = false).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun toggleFavorite_updatesButtonText() {
        composeTestRule.onNodeWithText("Give Me a Train Fact").performClick()

        // Check initial state (assuming not favorite)
        val addButton = composeTestRule.onNodeWithText("Add to Favorites", substring = true)
        addButton.assertExists()

        // Click Add to Favorites
        addButton.performClick()

        // Button should now say "In Favorites"
        composeTestRule.onNodeWithText("In Favorites", substring = true).assertExists()

        // Click again to remove
        composeTestRule.onNodeWithText("In Favorites", substring = true).performClick()

        // Should be back to Add to Favorites
        composeTestRule.onNodeWithText("Add to Favorites", substring = true).assertExists()
    }
}
