package com.erdees.foodcostcalc.ui.screens.settings

import android.icu.util.Currency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.settings.UserSettings
import com.erdees.foodcostcalc.ui.composables.NavigationListItem
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.Section
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.composables.labels.SectionLabel
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun SettingsScreen(navController: NavController) {

    val viewModel: SettingsViewModel = viewModel()

    val settings by viewModel.settingsModel.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val saveButtonEnabled by viewModel.saveButtonEnabled.collectAsState()
    val currencies by viewModel.currencies.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val settingsSaved = stringResource(id = R.string.settings_saved)

    LaunchedEffect(screenState) {
        when (screenState) {
            is ScreenState.Success -> {
                snackbarHostState.showSnackbar(
                    message = settingsSaved,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetScreenState()
            }

            else -> {}
        }
    }

    SettingsScreenContent(
        snackbarHostState,
        navController,
        settings,
        currencies,
        saveButtonEnabled,
        screenState,
        updateDefaultMargin = viewModel::updateDefaultMargin,
        updateDefaultTax = viewModel::updateDefaultTax,
        updateMetricUsed = viewModel::updateMetricUsed,
        updateImperialUsed = viewModel::updateImperialUsed,
        updateDefaultCurrencyCode = viewModel::updateDefaultCurrencyCode,
        saveSettings = viewModel::saveSettings,
        resetScreenState = viewModel::resetScreenState
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsScreenContent(
    snackbarHostState: SnackbarHostState,
    navController: NavController,
    settings: UserSettings,
    currencies: Set<Currency>,
    saveButtonEnabled: Boolean,
    screenState: ScreenState,
    updateDefaultTax: (String) -> Unit,
    updateDefaultMargin: (String) -> Unit,
    updateMetricUsed: (Boolean) -> Unit,
    updateImperialUsed: (Boolean) -> Unit,
    updateDefaultCurrencyCode: (Currency) -> Unit,
    saveSettings: () -> Unit,
    resetScreenState: () -> Unit
) {
    val scrollState = rememberScrollState()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = {
                    FCCTopAppBarNavIconButton(navController = navController)
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                Defaults(
                    settings,
                    currencies,
                    saveButtonEnabled,
                    updateDefaultMargin = updateDefaultMargin,
                    updateDefaultTax = updateDefaultTax,
                    updateMetricUsed = updateMetricUsed,
                    updateImperialUsed = updateImperialUsed,
                    updateDefaultCurrencyCode = updateDefaultCurrencyCode,
                    saveSettings = saveSettings
                )

                Spacer(modifier = Modifier.height(8.dp))

                OnlineDataBackup {
                    navController.navigate(FCCScreen.DataBackup)
                }

                Spacer(modifier = Modifier.height(8.dp))
                AppInformation()
            }



            when (screenState) {
                is ScreenState.Loading -> {
                    ScreenLoadingOverlay()
                }

                is ScreenState.Success -> {}

                is ScreenState.Error -> {
                    ErrorDialog {
                        resetScreenState()
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun AppInformation(modifier: Modifier = Modifier) {
    Section(modifier) {
        SectionLabel(
            text = stringResource(id = R.string.about),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FieldLabel(text = stringResource(id = R.string.app_version))
            Text(text = BuildConfig.VERSION_NAME)
        }

        Spacer(Modifier.height(4.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FieldLabel(text = stringResource(id = R.string.build_number))
            Text(text = BuildConfig.VERSION_CODE.toString())
        }

        Spacer(Modifier.height(4.dp))


        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FieldLabel(text = stringResource(id = R.string.developer))
            Text(text = stringResource(id = R.string.dev_signature))
        }

    }
}

@Composable
private fun OnlineDataBackup(
    modifier: Modifier = Modifier,
    onNavigateToOnlineDataBackup: () -> Unit
) {
    Section(modifier) {
        SectionLabel(text = stringResource(id = R.string.data), Modifier.padding(bottom = 4.dp))
        NavigationListItem(
            title = stringResource(id = R.string.data_backup),
            icon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.online),
                    contentDescription = stringResource(id = R.string.data_backup)
                )
            }) {
            onNavigateToOnlineDataBackup()
        }
    }
}

@Composable
private fun Defaults(
    settings: UserSettings,
    currencies: Set<Currency>,
    saveButtonEnabled: Boolean,
    modifier: Modifier = Modifier,
    updateDefaultTax: (String) -> Unit,
    updateDefaultMargin: (String) -> Unit,
    updateMetricUsed: (Boolean) -> Unit,
    updateImperialUsed: (Boolean) -> Unit,
    updateDefaultCurrencyCode: (Currency) -> Unit,
    saveSettings: () -> Unit
) {
    Section(modifier) {
        SectionLabel(text = stringResource(id = R.string.your_defaults), Modifier.padding(bottom = 4.dp))
        FCCTextField(
            title = stringResource(id = R.string.default_dish_tax),
            value = settings.defaultTax,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        ) {
            updateDefaultTax(it)
        }

        FCCTextField(
            title = stringResource(id = R.string.default_dish_margin),
            value = settings.defaultMargin,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        ) {
            updateDefaultMargin(it)
        }


        FieldLabel(text = stringResource(id = R.string.units_section))

        CheckBoxField(
            title = stringResource(id = R.string.use_metric_units),
            value = settings.metricUsed,
            onValueChange = { updateMetricUsed(it) }
        )

        CheckBoxField(
            title = stringResource(id = R.string.use_us_units),
            value = settings.imperialUsed,
            onValueChange = { updateImperialUsed(it) }
        )


        Column {
            FieldLabel(text = stringResource(id = R.string.default_currency))
            CurrenciesDropDown(
                currencies = currencies,
                selectedCurrency = settings.currency,
                selectCurrency = { updateDefaultCurrencyCode(it) }
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            FCCPrimaryButton(
                text = stringResource(id = R.string.save),
                enabled = saveButtonEnabled
            ) {
                saveSettings()
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

@Preview
@Composable
private fun PreviewSettingsScreenContent() {
    FCCTheme {
        SettingsScreenContent(
            snackbarHostState = SnackbarHostState(),
            navController = rememberNavController(),
            settings = UserSettings(
                defaultMargin = "10",
                defaultTax = "10",
                currency = Currency.getInstance("USD"),
                metricUsed = true,
                imperialUsed = false
            ),
            currencies = Currency.getAvailableCurrencies(),
            saveButtonEnabled = true,
            screenState = ScreenState.Idle,
            updateDefaultMargin = {},
            updateDefaultTax = {},
            updateMetricUsed = {},
            updateImperialUsed = {},
            updateDefaultCurrencyCode = {},
            saveSettings = {},
            resetScreenState = {}
        )
    }
}


@Preview
@Composable
fun PreviewDefaults() {
    FCCTheme {
        Defaults(
            settings = UserSettings(
                defaultMargin = "10",
                defaultTax = "10",
                currency = Currency.getInstance("USD"),
                metricUsed = true,
                imperialUsed = false
            ),
            currencies = Currency.getAvailableCurrencies(),
            saveButtonEnabled = true,
            updateDefaultTax = {},
            updateDefaultMargin = {},
            updateMetricUsed = {},
            updateImperialUsed = {},
            updateDefaultCurrencyCode = {},
        ) {

        }
    }
}