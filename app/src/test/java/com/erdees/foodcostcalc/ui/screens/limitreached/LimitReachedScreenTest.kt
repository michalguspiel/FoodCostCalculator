package com.erdees.foodcostcalc.ui.screens.limitreached

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for LimitReachedScreen composable.
 * Tests the UI components, callbacks, and content display.
 */
class LimitReachedScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun limitReachedScreen_displaysRequiredComponents() {
        // Arrange
        val testSubHeadline = "You've successfully created 20 dishes—the maximum for the free plan."
        
        // Act
        composeTestRule.setContent {
            FCCTheme {
                LimitReachedScreen(
                    subHeadline = testSubHeadline,
                    onDismiss = {},
                    onSeePremium = {}
                )
            }
        }
        
        // Assert
        composeTestRule.onNodeWithText("Wow, You're a Power User!").assertIsDisplayed()
        composeTestRule.onNodeWithText(testSubHeadline).assertIsDisplayed()
        composeTestRule.onNodeWithText("Upgrade to Premium to unlock unlimited dishes, cloud backup, and all professional features.").assertIsDisplayed()
        composeTestRule.onNodeWithText("See Premium Features").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Close").assertIsDisplayed()
    }

    @Test
    fun limitReachedScreen_closeButtonTriggersCallback() {
        // Arrange
        var dismissCalled = false
        val testSubHeadline = "Test message"
        
        // Act
        composeTestRule.setContent {
            FCCTheme {
                LimitReachedScreen(
                    subHeadline = testSubHeadline,
                    onDismiss = { dismissCalled = true },
                    onSeePremium = {}
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        
        // Assert
        assertEquals(true, dismissCalled)
    }

    @Test
    fun limitReachedScreen_ctaButtonTriggersCallback() {
        // Arrange
        var seePremiumCalled = false
        val testSubHeadline = "Test message"
        
        // Act
        composeTestRule.setContent {
            FCCTheme {
                LimitReachedScreen(
                    subHeadline = testSubHeadline,
                    onDismiss = {},
                    onSeePremium = { seePremiumCalled = true }
                )
            }
        }
        
        composeTestRule.onNodeWithText("See Premium Features").performClick()
        
        // Assert
        assertEquals(true, seePremiumCalled)
    }

    @Test
    fun limitReachedScreen_displaysCustomSubHeadline() {
        // Arrange
        val customMessage = "You've successfully created 5 custom items—the maximum for the free plan."
        
        // Act
        composeTestRule.setContent {
            FCCTheme {
                LimitReachedScreen(
                    subHeadline = customMessage,
                    onDismiss = {},
                    onSeePremium = {}
                )
            }
        }
        
        // Assert
        composeTestRule.onNode(hasText(customMessage)).assertIsDisplayed()
    }

    @Test
    fun limitReachedScreen_displaysStaticTexts() {
        // Act
        composeTestRule.setContent {
            FCCTheme {
                LimitReachedScreen(
                    subHeadline = "Test",
                    onDismiss = {},
                    onSeePremium = {}
                )
            }
        }
        
        // Assert - Check all static text elements
        composeTestRule.onNodeWithText("Wow, You're a Power User!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upgrade to Premium to unlock unlimited dishes, cloud backup, and all professional features.").assertIsDisplayed()
        composeTestRule.onNodeWithText("See Premium Features").assertIsDisplayed()
    }
}