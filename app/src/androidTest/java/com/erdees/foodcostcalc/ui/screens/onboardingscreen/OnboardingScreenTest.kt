package com.erdees.foodcostcalc.ui.screens.onboardingscreen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.ui.FCCActivity
import io.mockk.coVerify
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule

class OnboardingScreenTest {

class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FCCActivity>()

    // Mock Preferences
    private val mockPreferences: Preferences = mockk(relaxed = true)
    private val onboardingCompletedFlow = MutableStateFlow(false)

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single<Preferences>(override = true) { mockPreferences }
            }
        )
    }

    @Test
    fun onboardingScreen_isShown_whenNotCompleted() {
        every { mockPreferences.onboardingCompleted } returns onboardingCompletedFlow
        onboardingCompletedFlow.value = false

        // Activity will be launched by the rule, picking up the Koin module.
        // If FCCActivity is already running, might need composeTestRule.activityRule.scenario.recreate()
        // but usually KoinTestRule handles this if activity starts after rule.

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.welcome_to_app))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.complete_onboarding))
            .assertIsDisplayed()
    }

    @Test
    fun completeOnboardingButton_setsPreference_andNavigatesToHostScreen() {
        every { mockPreferences.onboardingCompleted } returns onboardingCompletedFlow
        onboardingCompletedFlow.value = false

        // composeTestRule.activityRule.scenario.recreate() // Ensure activity starts fresh with mock

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.complete_onboarding))
            .performClick()

        // Let the coroutine in FCCActivity run to update preferences and trigger recomposition
        composeTestRule.waitForIdle()

        // Verify that preferences were updated
        coVerify { mockPreferences.setOnboardingCompleted(true) }

        // Update the flow to simulate the change that would trigger recomposition in FCCActivity
        onboardingCompletedFlow.value = true
        composeTestRule.waitForIdle()


        // Verify that FCCHostScreen is displayed.
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.products), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun hostScreen_isShown_whenOnboardingAlreadyCompleted() {
        every { mockPreferences.onboardingCompleted } returns onboardingCompletedFlow
        onboardingCompletedFlow.value = true

        // Recreate activity to ensure it picks up the new 'true' value from the flow
        // initialized by KoinTestRule and mockPreferences.
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()


        // Verify that FCCHostScreen is displayed.
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.products), useUnmergedTree = true)
            .assertIsDisplayed()

        // Verify OnboardingScreen is NOT displayed
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.welcome_to_app))
            .assertDoesNotExist()
    }
}
