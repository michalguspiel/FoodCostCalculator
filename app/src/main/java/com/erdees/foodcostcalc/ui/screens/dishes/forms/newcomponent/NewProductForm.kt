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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
fun NewProductForm(
    state: NewProductFormUiState,
    actions: NewProductFormActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val title = when (state.currentStep) {
            NewProductWizardStep.DEFINE_PURCHASE -> stringResource(
                id = R.string.create_add_product_title,
                state.productName
            )
            NewProductWizardStep.DEFINE_USAGE -> stringResource(
                id = R.string.add_product_to_dish_title,
                state.productName,
                state.dishName
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
    val scrollState = rememberScrollState()
    val priceFocusRequester = remember { FocusRequester() }
    val quantityFocusRequester = remember { FocusRequester() }
    val unitFocusRequester = remember { FocusRequester() }
    val wasteFocusRequester = remember { FocusRequester() }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(scrollState)
    ) {
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

        FCCTextField(
            modifier = Modifier.focusRequester(wasteFocusRequester),
            title = stringResource(id = R.string.percent_of_waste),
            value = state.formData.wastePercent,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            onValueChange = { newWaste ->
                val sanitizedValue = onNumericValueChange(state.formData.wastePercent, newWaste)
                actions.onFormDataUpdate(state.formData.copy(wastePercent = sanitizedValue))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ButtonRow(
            modifier = Modifier.fillMaxWidth(),
            applyDefaultPadding = false,
            primaryButton = {
                FCCPrimaryButton(
                    text = stringResource(id = R.string.next_add_to_dish),
                    enabled = state.isNextButtonEnabled,
                    onClick = actions.onNextStep,
                    modifier = Modifier.weight(1f)
                )
            },
            secondaryButton = {
                FCCTextButton(
                    text = stringResource(id = R.string.cancel),
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
    val scrollState = rememberScrollState()
    val quantityFocusRequester = remember { FocusRequester() }
    val unitFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.how_much_using_in_dish),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FCCTextField(
            value = state.formData.quantityAddedToDish,
            title = stringResource(id = R.string.quantity),
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
            label = stringResource(id = R.string.unit_for_dish),
            units = state.productAdditionUnits,
            expanded = state.productAdditionDropdownExpanded,
            onExpandChange = actions.onProductAdditionDropdownExpandedChange,
            selectedUnit = state.formData.quantityAddedToDishUnit,
            selectUnit = { unit ->
                actions.onFormDataUpdate(state.formData.copy(quantityAddedToDishUnit = unit))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ButtonRow(
            modifier = Modifier.fillMaxWidth(),
            applyDefaultPadding = false,
            primaryButton = {
                FCCPrimaryButton(
                    text = stringResource(id = R.string.create_and_add_ingredient),
                    enabled = state.isCreateButtonEnabled,
                    onClick = { actions.onSaveProduct(state.formData) },
                    modifier = Modifier.weight(1f)
                )
            },
            secondaryButton = {
                FCCTextButton(
                    text = stringResource(id = R.string.back),
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
    val scrollState = rememberScrollState()
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        FCCTextField(
            title = stringResource(id = R.string.package_price),
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

        val packageQuantityText = state.formData.packageUnit?.symbolRes?.let {
            stringResource(R.string.package_quantity, stringResource(it))
        } ?: stringResource(R.string.package_quantity_empty)
        FCCTextField(
            title = packageQuantityText,
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
            label = stringResource(id = R.string.package_unit),
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
            title = stringResource(id = R.string.unit_price),
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
            label = stringResource(id = R.string.unit_price_unit),
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


@Preview(showBackground = true, name = "Define Purchase Step")
@PreviewLightDark
@Composable
private fun DefinePurchaseStepPreview() {
    FCCTheme {
        NewProductForm(
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
            actions = NewProductFormActions.Empty
        )
    }
}

@Preview(showBackground = true, name = "Define Usage Step")
@PreviewLightDark
@Composable
private fun DefineUsageStepPreview() {
    FCCTheme {
        NewProductForm(
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
            actions = NewProductFormActions.Empty
        )
    }
}