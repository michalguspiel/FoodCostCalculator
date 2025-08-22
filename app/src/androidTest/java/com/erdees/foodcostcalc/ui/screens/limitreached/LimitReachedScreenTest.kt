package com.erdees.foodcostcalc.ui.screens.limitreached

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import org.junit.Assert
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
        var context : Context? = null
        // Act
        composeTestRule.setContent {
            context = LocalContext.current
            FCCTheme {
                LimitReachedScreen(
                    subHeadline = testSubHeadline,
                    onDismiss = {},
                    onSeePremium = {}
                )
            }
        }

        context ?: throw IllegalStateException("Context is null")

        // Assert
        composeTestRule.onNodeWithText(context.getString(R.string.limit_reached_title))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(testSubHeadline).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.limit_reached_description))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.limit_reached_button_text))
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.limit_reached_close_description))
            .assertIsDisplayed()
    }

    @Test
    fun limitReachedScreen_closeButtonTriggersCallback() {
        // Arrange
        var dismissCalled = false
        val testSubHeadline = "Test message"
        var context : Context? = null

        // Act
        composeTestRule.setContent {
            context = LocalContext.current
            FCCTheme {
                LimitReachedScreen(
                    subHeadline = testSubHeadline,
                    onDismiss = { dismissCalled = true },
                    onSeePremium = {}
                )
            }
        }

        context ?: throw IllegalStateException("Context is null")


        composeTestRule.onNodeWithContentDescription(context.getString(R.string.limit_reached_close_description)).performClick()

        // Assert
        Assert.assertEquals(true, dismissCalled)
    }

    @Test
    fun limitReachedScreen_ctaButtonTriggersCallback() {
        // Arrange
        var seePremiumCalled = false
        val testSubHeadline = "Test message"
        var context : Context? = null

        // Act
        composeTestRule.setContent {
            context = LocalContext.current
            FCCTheme {
                LimitReachedScreen(
                    subHeadline = testSubHeadline,
                    onDismiss = {},
                    onSeePremium = { seePremiumCalled = true }
                )
            }
        }
        context ?: throw IllegalStateException("Context is null")
        composeTestRule.onNodeWithText(context.getString(R.string.limit_reached_button_text))
            .performClick()

        // Assert
        Assert.assertEquals(true, seePremiumCalled)
    }

    @Test
    fun limitReachedScreen_displaysCustomSubHeadline() {
        // Arrange
        val customMessage =
            "You've successfully created 5 custom items—the maximum for the free plan."

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
}