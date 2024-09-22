package com.erdees.foodcostcalc.ui.screens.settings

import android.icu.util.Currency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {

    val viewModel: SettingsViewModel = viewModel()

    val settings by viewModel.settingsModel.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val saveButtonEnabled by viewModel.saveButtonEnabled.collectAsState()
    val currencies by viewModel.currencies.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success -> {
                snackbarHostState.showSnackbar("Settings saved", duration = SnackbarDuration.Short)
                viewModel.resetScreenState()
            }

            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    FCCTopAppBarNavIconButton(navController = navController)
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp)
                    .padding(horizontal = 12.dp)
            ) {

                Column(
                    Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FCCTextField(
                        title = stringResource(id = R.string.default_dish_tax),
                        value = settings.defaultTax,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    ) {
                        viewModel.updateDefaultTax(it)
                    }

                    FCCTextField(
                        title = stringResource(id = R.string.default_dish_margin),
                        value = settings.defaultMargin,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    ) {
                        viewModel.updateDefaultMargin(it)
                    }


                    FieldLabel(text = stringResource(id = R.string.units_section))

                    CheckBoxField(
                        title = stringResource(id = R.string.use_metric_units),
                        value = settings.metricUsed,
                        onValueChange = viewModel::updateMetricUsed
                    )

                    CheckBoxField(
                        title = stringResource(id = R.string.use_us_units),
                        value = settings.imperialUsed,
                        onValueChange = viewModel::updateImperialUsed
                    )


                    Column {
                        FieldLabel(text = stringResource(id = R.string.default_currency))
                        CurrenciesDropDown(
                            currencies = currencies,
                            selectedCurrency = settings.currency,
                            selectCurrency = viewModel::updateDefaultCurrencyCode
                        )
                    }
                }


                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        stringResource(
                            id = R.string.app_info_template,
                            BuildConfig.VERSION_NAME,
                            "Michał Guśpiel"
                        ),
                        style = MaterialTheme.typography.labelSmall
                    )


                    FCCPrimaryButton(
                        text = stringResource(id = R.string.save),
                        enabled = saveButtonEnabled
                    ) {
                        viewModel.saveSettings()
                    }
                }
            }



            when (screenState) {
                is ScreenState.Loading -> {
                    ScreenLoadingOverlay()
                }

                is ScreenState.Success -> {}

                is ScreenState.Error -> {
                    ErrorDialog {
                        viewModel.resetScreenState()
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun CheckBoxField(
    title: String,
    value: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Checkbox(
            checked = value,
            onCheckedChange = { onValueChange(it) }
        )
    }
}

@Preview
@Composable
private fun PreviewCheckBoxField() {
    CheckBoxField(
        title = "Use metric units",
        value = true,
        onValueChange = {}
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrenciesDropDown(
    currencies: Set<Currency>,
    selectedCurrency: Currency,
    modifier: Modifier = Modifier,
    selectCurrency: (Currency) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                value = selectedCurrency.displayName,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(focusedPlaceholderColor = Color.Transparent),
            )

            DropdownMenu(
                modifier = Modifier.exposedDropdownSize(true),
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                currencies.forEach { unit ->
                    DropdownMenuItem(onClick = {
                        selectCurrency(unit)
                        expanded = false
                    }, text = {
                        Text(unit.displayName)
                    })
                }
            }
        }
    }
}