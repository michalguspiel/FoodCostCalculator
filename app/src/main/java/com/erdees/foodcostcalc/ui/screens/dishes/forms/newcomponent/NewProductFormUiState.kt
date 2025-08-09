package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

data class NewProductFormUiState(
    val productName: String = "",
    val dishName: String = "",

    // Wizard state
    val currentStep: NewProductWizardStep,

    // Units and dropdowns
    val productCreationUnits: Set<MeasurementUnit> = emptySet(),
    val productAdditionUnits: Set<MeasurementUnit> = emptySet(),
    val productCreationDropdownExpanded: Boolean = false,
    val productAdditionDropdownExpanded: Boolean = false,

    // Form data
    val formData: NewProductFormData = NewProductFormData(),

    // Button states
    val isNextButtonEnabled: Boolean = false,
    val isCreateButtonEnabled: Boolean = false,
)