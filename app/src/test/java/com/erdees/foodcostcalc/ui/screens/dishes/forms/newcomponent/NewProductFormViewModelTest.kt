package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.Utils
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@ExperimentalCoroutinesApi
class NewProductFormViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var preferences: Preferences
    private lateinit var viewModel: NewProductFormViewModel

    private val testModule = module {
        single<Preferences> { preferences }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        preferences = mockk(relaxed = true)

        // Setup default preferences
        every { preferences.metricUsed } returns flowOf(true)
        every { preferences.imperialUsed } returns flowOf(false)
        every { preferences.showProductTax } returns MutableStateFlow(true)

        startKoin {
            modules(testModule)
        }
    }

    private suspend fun TestScope.initializeViewModel() {
        viewModel = NewProductFormViewModel()

        // Collect flows early to ensure they're hot
        viewModel.formData.first()
        viewModel.currentStep.first()
        viewModel.productCreationUnits.first()
        viewModel.productAdditionUnits.first()
        viewModel.isNextButtonEnabled.first()
        viewModel.isCreateButtonEnabled.first()
        advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `initial state should have default values`() = runTest {
        initializeViewModel()

        val initialFormData = viewModel.formData.first()
        initialFormData.inputMethod shouldBe InputMethod.PACKAGE
        initialFormData.packagePrice shouldBe ""
        initialFormData.packageQuantity shouldBe ""
        initialFormData.packageUnit shouldBe MeasurementUnit.KILOGRAM
        initialFormData.unitPrice shouldBe ""
        initialFormData.unitPriceUnit shouldBe MeasurementUnit.KILOGRAM
        initialFormData.wastePercent shouldBe ""
        initialFormData.quantityAddedToDish shouldBe ""
        initialFormData.quantityAddedToDishUnit shouldBe null

        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE
        viewModel.productCreationDropdownExpanded.first() shouldBe false
        viewModel.productAdditionDropdownExpanded.first() shouldBe false
    }

    @Test
    fun `updateFormData should update package pricing fields`() = runTest {
        initializeViewModel()

        val updatedFormData = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packagePrice = "15.99",
            packageQuantity = "2.5",
            packageUnit = MeasurementUnit.GRAM,
            wastePercent = "5.0"
        )

        viewModel.updateFormData(updatedFormData)
        advanceUntilIdle()

        val formData = viewModel.formData.first()
        formData.packagePrice shouldBe "15.99"
        formData.packageQuantity shouldBe "2.5"
        formData.packageUnit shouldBe MeasurementUnit.GRAM
        formData.wastePercent shouldBe "5.0"
    }

    @Test
    fun `updateFormData should update unit pricing fields`() = runTest {
        initializeViewModel()

        val updatedFormData = NewProductFormData(
            inputMethod = InputMethod.UNIT,
            unitPrice = "8.50",
            unitPriceUnit = MeasurementUnit.LITER,
            wastePercent = "3.0"
        )

        viewModel.updateFormData(updatedFormData)
        advanceUntilIdle()

        val formData = viewModel.formData.first()
        formData.inputMethod shouldBe InputMethod.UNIT
        formData.unitPrice shouldBe "8.50"
        formData.unitPriceUnit shouldBe MeasurementUnit.LITER
        formData.wastePercent shouldBe "3.0"
    }

    @Test
    fun `updateFormData should update wizard-specific fields`() = runTest {
        initializeViewModel()

        val updatedFormData = NewProductFormData(
            quantityAddedToDish = "200",
            quantityAddedToDishUnit = MeasurementUnit.GRAM
        )

        viewModel.updateFormData(updatedFormData)
        advanceUntilIdle()

        val formData = viewModel.formData.first()
        formData.quantityAddedToDish shouldBe "200"
        formData.quantityAddedToDishUnit shouldBe MeasurementUnit.GRAM
    }

    @Test
    fun `goToNextStep should transition from DEFINE_PURCHASE to DEFINE_USAGE`() = runTest {
        initializeViewModel()

        // Initially should be on first step
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE

        viewModel.goToNextStep()
        advanceUntilIdle()

        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_USAGE
    }

    @Test
    fun `goToNextStep should not transition beyond DEFINE_USAGE`() = runTest {
        initializeViewModel()

        // Go to usage step
        viewModel.goToNextStep()
        advanceUntilIdle()
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_USAGE

        // Try to go next again - should stay on usage step
        viewModel.goToNextStep()
        advanceUntilIdle()
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_USAGE
    }

    @Test
    fun `goToPreviousStep should transition from DEFINE_USAGE to DEFINE_PURCHASE`() = runTest {
        initializeViewModel()

        // Go to usage step first
        viewModel.goToNextStep()
        advanceUntilIdle()
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_USAGE

        // Go back
        viewModel.goToPreviousStep()
        advanceUntilIdle()
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE
    }

    @Test
    fun `goToPreviousStep should not transition before DEFINE_PURCHASE`() = runTest {
        initializeViewModel()

        // Already on first step
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE

        // Try to go previous - should stay on first step
        viewModel.goToPreviousStep()
        advanceUntilIdle()
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE
    }

    @Test
    fun `resetToFirstStep should always go to DEFINE_PURCHASE`() = runTest {
        initializeViewModel()

        // Go to usage step
        viewModel.goToNextStep()
        advanceUntilIdle()
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_USAGE

        // Reset to first step
        viewModel.resetToFirstStep()
        advanceUntilIdle()
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE
    }

    @Test
    fun `goToNextStep should auto-set dish unit when purchase unit changes category`() = runTest {
        initializeViewModel()

        // Set package unit to liter (volume category)
        val packageFormData = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packageUnit = MeasurementUnit.LITER
        )
        viewModel.updateFormData(packageFormData)
        advanceUntilIdle()

        // Go to next step
        viewModel.goToNextStep()
        advanceUntilIdle()

        // Should auto-set dish unit to a volume unit from available units
        val formData = viewModel.formData.first()
        formData.quantityAddedToDishUnit?.category shouldBe MeasurementUnit.LITER.category
    }

    @Test
    fun `productCreationUnits should return units from preferences`() = runTest {
        initializeViewModel()

        val units = viewModel.productCreationUnits.first()
        val expectedUnits = Utils.getCompleteUnitsSet(true, false)
        units shouldBe expectedUnits
    }

    @Test
    fun `productAdditionUnits should filter by purchase unit category`() = runTest {
        initializeViewModel()

        // Set package unit to liter (volume category)
        val packageFormData = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packageUnit = MeasurementUnit.LITER
        )
        viewModel.updateFormData(packageFormData)
        advanceUntilIdle()

        val additionUnits = viewModel.productAdditionUnits.first()
        // Should only contain volume units
        additionUnits.all { it.category == MeasurementUnit.LITER.category } shouldBe true
    }

    @Test
    fun `setProductCreationDropdownExpanded should update dropdown state`() = runTest {
        initializeViewModel()

        // Initially collapsed
        viewModel.productCreationDropdownExpanded.first() shouldBe false

        viewModel.setProductCreationDropdownExpanded(true)
        advanceUntilIdle()
        viewModel.productCreationDropdownExpanded.first() shouldBe true

        viewModel.setProductCreationDropdownExpanded(false)
        advanceUntilIdle()
        viewModel.productCreationDropdownExpanded.first() shouldBe false
    }

    @Test
    fun `setProductAdditionDropdownExpanded should update dropdown state`() = runTest {
        initializeViewModel()

        // Initially collapsed
        viewModel.productAdditionDropdownExpanded.first() shouldBe false

        viewModel.setProductAdditionDropdownExpanded(true)
        advanceUntilIdle()
        viewModel.productAdditionDropdownExpanded.first() shouldBe true

        viewModel.setProductAdditionDropdownExpanded(false)
        advanceUntilIdle()
        viewModel.productAdditionDropdownExpanded.first() shouldBe false
    }

    @Test
    fun `isNextButtonEnabled should be false when validation fails`() = runTest {
        initializeViewModel()

        // Collect flow early
        val isNextEnabled = viewModel.isNextButtonEnabled

        // On first step with empty form data
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE
        isNextEnabled.first() shouldBe false
    }

    @Test
    fun `isNextButtonEnabled should be true when step 1 validation passes`() = runTest {
        initializeViewModel()

        // Collect flow early
        val isNextEnabled = viewModel.isNextButtonEnabled

        // Set valid package data
        val validFormData = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packagePrice = "10.0",
            packageQuantity = "2.0",
            packageUnit = MeasurementUnit.KILOGRAM
        )
        viewModel.updateFormData(validFormData)
        advanceUntilIdle()

        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE
        isNextEnabled.first() shouldBe true
    }

    @Test
    fun `isNextButtonEnabled should be false when not on first step`() = runTest {
        initializeViewModel()

        // Collect flow early
        val isNextEnabled = viewModel.isNextButtonEnabled

        // Go to second step
        viewModel.goToNextStep()
        advanceUntilIdle()

        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_USAGE
        isNextEnabled.first() shouldBe false
    }

    @Test
    fun `isCreateButtonEnabled should be false when validation fails`() = runTest {
        initializeViewModel()

        // Collect flow early
        val isCreateEnabled = viewModel.isCreateButtonEnabled

        // Initially false
        isCreateEnabled.first() shouldBe false

        // Go to step 2 but with invalid data
        viewModel.goToNextStep()
        advanceUntilIdle()
        isCreateEnabled.first() shouldBe false
    }

    @Test
    fun `isCreateButtonEnabled should be true when all validations pass on step 2`() = runTest {
        initializeViewModel()

        // Collect flow early
        val isCreateEnabled = viewModel.isCreateButtonEnabled

        // Set valid step 1 data
        val step1Data = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packagePrice = "10.0",
            packageQuantity = "2.0",
            packageUnit = MeasurementUnit.KILOGRAM
        )
        viewModel.updateFormData(step1Data)
        advanceUntilIdle()

        // Go to step 2
        viewModel.goToNextStep()
        advanceUntilIdle()

        // Set valid step 2 data
        val step2Data = step1Data.copy(
            quantityAddedToDish = "500",
            quantityAddedToDishUnit = MeasurementUnit.GRAM
        )
        viewModel.updateFormData(step2Data)
        advanceUntilIdle()

        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_USAGE
        isCreateEnabled.first() shouldBe true
    }

    @Test
    fun `isCreateButtonEnabled should be false when not on step 2`() = runTest {
        initializeViewModel()

        // Collect flow early
        val isCreateEnabled = viewModel.isCreateButtonEnabled

        // Even with valid data, should be false on step 1
        val validData = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packagePrice = "10.0",
            packageQuantity = "2.0",
            packageUnit = MeasurementUnit.KILOGRAM,
            quantityAddedToDish = "500",
            quantityAddedToDishUnit = MeasurementUnit.GRAM
        )
        viewModel.updateFormData(validData)
        advanceUntilIdle()

        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE
        isCreateEnabled.first() shouldBe false
    }

    @Test
    fun `onAddIngredientClick should reset all state to defaults`() = runTest {
        initializeViewModel()

        // Set some data and go to step 2
        val formData = NewProductFormData(
            inputMethod = InputMethod.UNIT,
            unitPrice = "15.0",
            unitPriceUnit = MeasurementUnit.LITER,
            wastePercent = "10.0",
            quantityAddedToDish = "300",
            quantityAddedToDishUnit = MeasurementUnit.MILLILITER
        )
        viewModel.updateFormData(formData)
        viewModel.goToNextStep()
        viewModel.setProductCreationDropdownExpanded(true)
        viewModel.setProductAdditionDropdownExpanded(true)
        advanceUntilIdle()

        // Verify state is changed
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_USAGE
        viewModel.formData.first().unitPrice shouldBe "15.0"
        viewModel.productCreationDropdownExpanded.first() shouldBe true

        // Reset
        viewModel.onAddIngredientClick()
        advanceUntilIdle()

        // Verify everything is reset
        viewModel.currentStep.first() shouldBe NewProductWizardStep.DEFINE_PURCHASE

        val resetFormData = viewModel.formData.first()
        resetFormData.inputMethod shouldBe InputMethod.PACKAGE
        resetFormData.packagePrice shouldBe ""
        resetFormData.packageQuantity shouldBe ""
        resetFormData.packageUnit shouldBe MeasurementUnit.KILOGRAM
        resetFormData.unitPrice shouldBe ""
        resetFormData.unitPriceUnit shouldBe MeasurementUnit.KILOGRAM
        resetFormData.wastePercent shouldBe ""
        resetFormData.quantityAddedToDish shouldBe ""
        resetFormData.quantityAddedToDishUnit shouldBe null

        // Dropdown states should remain (they're not reset in onAddIngredientClick)
        viewModel.productCreationDropdownExpanded.first() shouldBe true
        viewModel.productAdditionDropdownExpanded.first() shouldBe true
    }

    @Test
    fun `formData should reactively combine bridge delegate state with local wizard state`() = runTest {
        initializeViewModel()

        // Update package pricing through form data
        val packageData = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packagePrice = "20.0",
            packageQuantity = "1.5"
        )
        viewModel.updateFormData(packageData)
        advanceUntilIdle()

        // Update wizard-specific fields
        val wizardData = packageData.copy(
            quantityAddedToDish = "750",
            quantityAddedToDishUnit = MeasurementUnit.GRAM
        )
        viewModel.updateFormData(wizardData)
        advanceUntilIdle()

        // Verify combined state
        val combinedData = viewModel.formData.first()
        combinedData.packagePrice shouldBe "20.0"
        combinedData.packageQuantity shouldBe "1.5"
        combinedData.quantityAddedToDish shouldBe "750"
        combinedData.quantityAddedToDishUnit shouldBe MeasurementUnit.GRAM
    }

    @Test
    fun `productAdditionUnits should update when purchase unit changes`() = runTest {
        initializeViewModel()

        // Start with weight unit
        val weightFormData = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packageUnit = MeasurementUnit.KILOGRAM
        )
        viewModel.updateFormData(weightFormData)
        advanceUntilIdle()

        val weightUnits = viewModel.productAdditionUnits.first()
        weightUnits.all { it.category == MeasurementUnit.KILOGRAM.category } shouldBe true

        // Change to volume unit
        val volumeFormData = weightFormData.copy(
            packageUnit = MeasurementUnit.LITER
        )
        viewModel.updateFormData(volumeFormData)
        advanceUntilIdle()

        val volumeUnits = viewModel.productAdditionUnits.first()
        volumeUnits.all { it.category == MeasurementUnit.LITER.category } shouldBe true
    }

    @Test
    fun `form data state should handle input method transitions correctly`() = runTest {
        initializeViewModel()

        // Start with package data
        val packageData = NewProductFormData(
            inputMethod = InputMethod.PACKAGE,
            packagePrice = "12.0",
            packageQuantity = "3.0",
            packageUnit = MeasurementUnit.KILOGRAM
        )
        viewModel.updateFormData(packageData)
        advanceUntilIdle()

        var formData = viewModel.formData.first()
        formData.packagePrice shouldBe "12.0"
        formData.unitPrice shouldBe ""

        // Switch to unit pricing
        val unitData = packageData.copy(
            inputMethod = InputMethod.UNIT,
            unitPrice = "8.0",
            unitPriceUnit = MeasurementUnit.GRAM
        )
        viewModel.updateFormData(unitData)
        advanceUntilIdle()

        formData = viewModel.formData.first()
        formData.inputMethod shouldBe InputMethod.UNIT
        formData.unitPrice shouldBe "8.0"
        formData.packagePrice shouldBe "12.0" // Previous data should be preserved
    }
}
