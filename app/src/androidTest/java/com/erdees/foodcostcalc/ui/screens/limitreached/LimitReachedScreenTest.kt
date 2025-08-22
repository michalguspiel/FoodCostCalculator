package com.erdees.foodcostcalc.ui.screens.limitreached

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
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

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun setLimitReachedScreenContent(
        subHeadline: String,
        onDismiss: () -> Unit = {},
        onSeePremium: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            FCCTheme {
                LimitReachedScreen(
                    subHeadline = subHeadline,
                    onDismiss = onDismiss,
                    onSeePremium = onSeePremium
                )
            }
        }
    }

    @Test
    fun limitReachedScreen_displaysRequiredComponents() {
        // Arrange
        val testSubHeadline = "You've successfully created 20 dishes—the maximum for the free plan."

        // Act
        setLimitReachedScreenContent(subHeadline = testSubHeadline)

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

        // Act
        setLimitReachedScreenContent(
            subHeadline = testSubHeadline,
            onDismiss = { dismissCalled = true }
        )

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.limit_reached_close_description))
            .performClick()

        // Assert
        Assert.assertTrue("Dismiss callback should be called", dismissCalled)
    }

    @Test
    fun limitReachedScreen_ctaButtonTriggersCallback() {
        // Arrange
        var seePremiumCalled = false
        val testSubHeadline = "Test message"

        // Act
        setLimitReachedScreenContent(
            subHeadline = testSubHeadline,
            onSeePremium = { seePremiumCalled = true }
        )

        composeTestRule.onNodeWithText(context.getString(R.string.limit_reached_button_text))
            .performClick()

        // Assert
        Assert.assertTrue("See premium callback should be called", seePremiumCalled)
    }

    @Test
    fun limitReachedScreen_displaysCustomSubHeadline() {
        // Arrange
        val customMessage = "You've successfully created 5 custom items—the maximum for the free plan."

        // Act
        setLimitReachedScreenContent(subHeadline = customMessage)

        // Assert
        composeTestRule.onNode(hasText(customMessage)).assertIsDisplayed()
    }
}