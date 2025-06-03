package com.erdees.foodcostcalc.ui.viewModel

import app.cash.turbine.test
import com.erdees.foodcostcalc.domain.usecase.SubmitFeatureRequestUseCase
import com.erdees.foodcostcalc.ui.screens.featureRequest.FeatureRequestScreenState
import com.erdees.foodcostcalc.ui.screens.featureRequest.FeatureRequestViewModel
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

@ExperimentalCoroutinesApi
class FeatureRequestViewModelTest : KoinTest {

    private lateinit var viewModel: FeatureRequestViewModel
    private lateinit var submitFeatureRequestUseCase: SubmitFeatureRequestUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        submitFeatureRequestUseCase = mockk()
        startKoin {
            modules(module {
                single { submitFeatureRequestUseCase }
            })
        }
        viewModel = FeatureRequestViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `initial state should be IDLE with empty title and description`() = runTest {
        viewModel.screenState.value shouldBe FeatureRequestScreenState.IDLE
        viewModel.title.value shouldBe ""
        viewModel.description.value shouldBe ""
        viewModel.isSubmitButtonEnabled.value shouldBe false
    }

    @Test
    fun `updateTitle should update title`() = runTest {
        val newTitle = "New Title"
        viewModel.updateTitle(newTitle)
        viewModel.title.value shouldBe newTitle
    }

    @Test
    fun `updateDescription should update description`() = runTest {
        val newDescription = "New Description"
        viewModel.updateDescription(newDescription)
        viewModel.description.value shouldBe newDescription
    }

    @Test
    fun `isSubmitButtonEnabled should be true when title and description are not blank and state is not LOADING`() = runTest {
        viewModel.isSubmitButtonEnabled.test {
            awaitItem() shouldBe false // Initial state
            viewModel.updateTitle("Title")
            awaitItem() shouldBe false // Description still blank
            viewModel.updateDescription("Description")
            awaitItem() shouldBe true // Both filled
        }
    }

    @Test
    fun `isSubmitButtonEnabled should be false when state is LOADING`() = runTest {
         coEvery { submitFeatureRequestUseCase.invoke(any(), any()) } coAnswers {
            viewModel.screenState.value shouldBe FeatureRequestScreenState.LOADING
            viewModel.isSubmitButtonEnabled.value shouldBe false
            Result.success(Unit)
        }
        viewModel.updateTitle("Title")
        viewModel.updateDescription("Description")
        viewModel.isSubmitButtonEnabled.value shouldBe true // Enabled before submit
        viewModel.submitFeatureRequest()
        // Assertion is within the coEvery block for timing
        testDispatcher.scheduler.advanceUntilIdle() // Ensure coroutine completes
         viewModel.screenState.value shouldBe FeatureRequestScreenState.SUCCESS // Verify final state
    }


    @Test
    fun `submitFeatureRequest should move state to LOADING then SUCCESS on use case success`() = runTest {
        val title = "Test Title"
        val description = "Test Description"
        coEvery { submitFeatureRequestUseCase.invoke(title, description) } returns Result.success(Unit)

        viewModel.updateTitle(title)
        viewModel.updateDescription(description)

        viewModel.screenState.test {
            awaitItem() shouldBe FeatureRequestScreenState.IDLE
            viewModel.submitFeatureRequest()
            awaitItem() shouldBe FeatureRequestScreenState.LOADING
            testDispatcher.scheduler.advanceUntilIdle() // Advance time for the use case to complete
            awaitItem() shouldBe FeatureRequestScreenState.SUCCESS
        }
    }

    @Test
    fun `submitFeatureRequest should move state to LOADING then ERROR on use case failure`() = runTest {
        val title = "Test Title"
        val description = "Test Description"
        val exception = RuntimeException("Test error")
        coEvery { submitFeatureRequestUseCase.invoke(title, description) } returns Result.failure(exception)

        viewModel.updateTitle(title)
        viewModel.updateDescription(description)

        viewModel.screenState.test {
            awaitItem() shouldBe FeatureRequestScreenState.IDLE
            viewModel.submitFeatureRequest()
            awaitItem() shouldBe FeatureRequestScreenState.LOADING
            testDispatcher.scheduler.advanceUntilIdle() // Advance time for the use case to complete
            awaitItem() shouldBe FeatureRequestScreenState.ERROR
        }
    }

    @Test
    fun `resetScreenState should set screenState to IDLE`() = runTest {
        viewModel.submitFeatureRequest() // Change state first
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.resetScreenState()
        viewModel.screenState.value shouldBe FeatureRequestScreenState.IDLE
    }
}
