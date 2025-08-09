package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.buttons.TwoOptionToggle
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.fields.UnitField
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.onNumericValueChange

@Composable
fun NewProductWizard(
    state: NewProductFormUiState,
    actions: NewProductFormActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dynamic title based on current step
        val title = when (state.currentStep) {
            NewProductWizardStep.DEFINE_PURCHASE -> "Create & Add: ${state.productName}"
            NewProductWizardStep.DEFINE_USAGE -> "Add \"${state.productName}\" to \"${state.dishName}\""
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Animated content transition between steps
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
            },
            label = "wizard_step_transition"
        ) { step ->
            when (step) {
                NewProductWizardStep.DEFINE_PURCHASE -> {
                    DefinePurchaseStep(
                        state = state,
                        actions = actions
                    )
                }
                NewProductWizardStep.DEFINE_USAGE -> {
                    DefineUsageStep(
                        state = state,
                        actions = actions
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefinePurchaseStep(
    state: NewProductFormUiState,
    actions: NewProductFormActions
) {
    val priceFocusRequester = remember { FocusRequester() }
    val quantityFocusRequester = remember { FocusRequester() }
    val unitFocusRequester = remember { FocusRequester() }
    val wasteFocusRequester = remember { FocusRequester() }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pricing method toggle
        TwoOptionToggle(
            option1Text = stringResource(id = R.string.by_package),
            option2Text = stringResource(id = R.string.by_unit),
            selectedIndex = when (state.formData.inputMethod) {
                InputMethod.PACKAGE -> 0
                InputMethod.UNIT -> 1
            },
            onSelectionChange = { index ->
                val method = when (index) {
                    0 -> InputMethod.PACKAGE
                    1 -> InputMethod.UNIT
                    else -> InputMethod.PACKAGE
                }
                actions.onFormDataUpdate(state.formData.copy(inputMethod = method))
            }
        )

        // Conditional pricing forms
        when (state.formData.inputMethod) {
            InputMethod.PACKAGE -> {
                PackagePricingForm(
                    state = state,
                    actions = actions,
                    priceFocusRequester = priceFocusRequester,
                    quantityFocusRequester = quantityFocusRequester,
                    unitFocusRequester = unitFocusRequester,
                    wasteFocusRequester = wasteFocusRequester
                )
            }
            InputMethod.UNIT -> {
                UnitPricingForm(
                    state = state,
                    actions = actions,
                    priceFocusRequester = priceFocusRequester,
                    unitFocusRequester = unitFocusRequester,
                    wasteFocusRequester = wasteFocusRequester
                )
            }
        }

        // Waste field with calculate button
        WasteField(
            waste = state.formData.wastePercent,
            onWasteChange = { newWaste ->
                actions.onFormDataUpdate(state.formData.copy(wastePercent = newWaste))
            },
            onCalculateWaste = { /* TODO: Implement waste calculation */ },
            modifier = Modifier.focusRequester(wasteFocusRequester)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step 1 buttons
        ButtonRow(
            modifier = Modifier.fillMaxWidth(),
            applyDefaultPadding = false,
            primaryButton = {
                FCCPrimaryButton(
                    text = "Next: Add to Dish",
                    enabled = state.isNextButtonEnabled,
                    onClick = actions.onNextStep,
                    modifier = Modifier.weight(1f)
                )
            },
            secondaryButton = {
                FCCTextButton(
                    text = "Cancel",
                    onClick = actions.onCancel,
                    modifier = Modifier.weight(1f)
                )
            }
        )
    }
}

@Composable
private fun DefineUsageStep(
    state: NewProductFormUiState,
    actions: NewProductFormActions
) {
    val quantityFocusRequester = remember { FocusRequester() }
    val unitFocusRequester = remember { FocusRequester() }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How much are you using in this dish?",
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FCCTextField(
            value = state.formData.quantityAddedToDish,
            title = "Quantity for '${state.dishName}'",
            onValueChange = { newQuantity ->
                val sanitizedValue = onNumericValueChange(state.formData.quantityAddedToDish, newQuantity)
                actions.onFormDataUpdate(state.formData.copy(quantityAddedToDish = sanitizedValue))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                unitFocusRequester.requestFocus()
                actions.onProductAdditionDropdownExpandedChange(true)
            }),
            modifier = Modifier.focusRequester(quantityFocusRequester)
        )

        UnitField(
            modifier = Modifier.focusRequester(unitFocusRequester),
            label = "Unit for dish",
            units = state.productAdditionUnits,
            expanded = state.productAdditionDropdownExpanded,
            onExpandChange = actions.onProductAdditionDropdownExpandedChange,
            selectedUnit = state.formData.quantityAddedToDishUnit,
            selectUnit = { unit ->
                actions.onFormDataUpdate(state.formData.copy(quantityAddedToDishUnit = unit))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2 buttons
        ButtonRow(
            modifier = Modifier.fillMaxWidth(),
            applyDefaultPadding = false,
            primaryButton = {
                FCCPrimaryButton(
                    text = "Create & Add Ingredient",
                    enabled = state.isCreateButtonEnabled,
                    onClick = { actions.onSaveProduct(state.formData) },
                    modifier = Modifier.weight(1f)
                )
            },
            secondaryButton = {
                FCCTextButton(
                    text = "Back",
                    onClick = actions.onPreviousStep,
                    modifier = Modifier.weight(1f)
                )
            }
        )
    }
}


@Composable
private fun PackagePricingForm(
    state: NewProductFormUiState,
    actions: NewProductFormActions,
    priceFocusRequester: FocusRequester,
    quantityFocusRequester: FocusRequester,
    unitFocusRequester: FocusRequester,
    wasteFocusRequester: FocusRequester
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FCCTextField(
            title = "Package Price",
            value = state.formData.packagePrice,
            onValueChange = { newPrice ->
                val sanitizedValue = onNumericValueChange(state.formData.packagePrice, newPrice)
                actions.onFormDataUpdate(state.formData.copy(packagePrice = sanitizedValue))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                quantityFocusRequester.requestFocus()
            }),
            modifier = Modifier.focusRequester(priceFocusRequester)
        )

        FCCTextField(
            title = "Package Quantity",
            value = state.formData.packageQuantity,
            onValueChange = { newQuantity ->
                val sanitizedValue = onNumericValueChange(state.formData.packageQuantity, newQuantity)
                actions.onFormDataUpdate(state.formData.copy(packageQuantity = sanitizedValue))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                unitFocusRequester.requestFocus()
                actions.onProductCreationDropdownExpandedChange(true)
            }),
            modifier = Modifier.focusRequester(quantityFocusRequester)
        )

        UnitField(
            modifier = Modifier.focusRequester(unitFocusRequester),
            label = "Package Unit",
            expanded = state.productCreationDropdownExpanded,
            onExpandChange = actions.onProductCreationDropdownExpandedChange,
            units = state.productCreationUnits,
            selectedUnit = state.formData.packageUnit,
            selectUnit = { unit ->
                actions.onFormDataUpdate(state.formData.copy(packageUnit = unit))
                wasteFocusRequester.requestFocus()
            }
        )
    }
}

@Composable
private fun UnitPricingForm(
    state: NewProductFormUiState,
    actions: NewProductFormActions,
    priceFocusRequester: FocusRequester,
    unitFocusRequester: FocusRequester,
    wasteFocusRequester: FocusRequester
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FCCTextField(
            title = "Unit Price",
            value = state.formData.unitPrice,
            onValueChange = { newPrice ->
                val sanitizedValue = onNumericValueChange(state.formData.unitPrice, newPrice)
                actions.onFormDataUpdate(state.formData.copy(unitPrice = sanitizedValue))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                unitFocusRequester.requestFocus()
                actions.onProductCreationDropdownExpandedChange(true)
            }),
            modifier = Modifier.focusRequester(priceFocusRequester)
        )

        UnitField(
            modifier = Modifier.focusRequester(unitFocusRequester),
            label = "Unit Price Unit",
            expanded = state.productCreationDropdownExpanded,
            onExpandChange = actions.onProductCreationDropdownExpandedChange,
            units = state.productCreationUnits,
            selectedUnit = state.formData.unitPriceUnit,
            selectUnit = { unit ->
                actions.onFormDataUpdate(state.formData.copy(unitPriceUnit = unit))
                wasteFocusRequester.requestFocus()
            }
        )
    }
}

@Composable
private fun WasteField(
    waste: String,
    onWasteChange: (String) -> Unit,
    onCalculateWaste: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        FCCTextField(
            modifier = Modifier.weight(1f),
            title = "Waste % (Optional)",
            value = waste,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            onValueChange = { newWaste ->
                val sanitizedValue = onNumericValueChange(waste, newWaste)
                onWasteChange(sanitizedValue)
            }
        )

        FCCTextButton(
            text = "Calculate",
            onClick = onCalculateWaste
        )
    }
}

@Preview(showBackground = true, name = "Define Purchase Step")
@PreviewLightDark
@Composable
private fun DefinePurchaseStepPreview() {
    FCCTheme {
        NewProductWizard(
            state = NewProductFormUiState(
                productName = "Sugar",
                dishName = "Chocolate Cake",
                currentStep = NewProductWizardStep.DEFINE_PURCHASE,
                productCreationUnits = setOf(
                    MeasurementUnit.KILOGRAM,
                    MeasurementUnit.GRAM,
                    MeasurementUnit.LITER
                ),
                formData = NewProductFormData(
                    inputMethod = InputMethod.PACKAGE,
                    packagePrice = "12.99",
                    packageQuantity = "2.5",
                    packageUnit = MeasurementUnit.KILOGRAM
                ),
                isNextButtonEnabled = true
            ),
            actions = NewProductFormActions()
        )
    }
}

@Preview(showBackground = true, name = "Define Usage Step")
@PreviewLightDark
@Composable
private fun DefineUsageStepPreview() {
    FCCTheme {
        NewProductWizard(
            state = NewProductFormUiState(
                productName = "Sugar",
                dishName = "Chocolate Cake",
                currentStep = NewProductWizardStep.DEFINE_USAGE,
                productAdditionUnits = setOf(
                    MeasurementUnit.KILOGRAM,
                    MeasurementUnit.GRAM
                ),
                formData = NewProductFormData(
                    quantityAddedToDish = "200",
                    quantityAddedToDishUnit = MeasurementUnit.GRAM
                ),
                isCreateButtonEnabled = true
            ),
            actions = NewProductFormActions()
        )
    }
}