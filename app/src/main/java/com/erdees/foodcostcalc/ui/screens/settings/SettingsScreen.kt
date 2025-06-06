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
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme

data class SettingsScreenCallbacks(
    val updateDefaultTax: (String) -> Unit = {},
    val updateDefaultMargin: (String) -> Unit = {},
    val updateMetricUsed: (Boolean) -> Unit = {},
    val updateImperialUsed: (Boolean) -> Unit = {},
    val updateDefaultCurrencyCode: (Currency) -> Unit = {},
    val updateShowHalfProducts: (Boolean) -> Unit = {},
    val updateShowProductTax: (Boolean) -> Unit = {},
    val saveSettings: () -> Unit = {},
    val resetScreenState: () -> Unit = {},
)

@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = viewModel()) {


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
        settingsScreenCallbacks = SettingsScreenCallbacks(
            updateDefaultMargin = viewModel::updateDefaultMargin,
            updateDefaultTax = viewModel::updateDefaultTax,
            updateMetricUsed = viewModel::updateMetricUsed,
            updateImperialUsed = viewModel::updateImperialUsed,
            updateDefaultCurrencyCode = viewModel::updateDefaultCurrencyCode,
            updateShowHalfProducts = viewModel::updateShowHalfProducts,
            updateShowProductTax = viewModel::updateShowProductTax,
            saveSettings = viewModel::saveSettings,
            resetScreenState = viewModel::resetScreenState,
        )
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsScreenContent(
    snackbarHostState: SnackbarHostState,
    navController: NavController,
    settings: UserSettings?,
    currencies: Set<Currency>,
    saveButtonEnabled: Boolean,
    screenState: ScreenState,
    settingsScreenCallbacks: SettingsScreenCallbacks
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
        Box(modifier = Modifier.padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {

                Defaults(
                    settings,
                    currencies,
                    saveButtonEnabled,
                    settingsScreenCallbacks
                )

                AccountServicesSection(
                    onPremiumClick = { navController.navigate(FCCScreen.Subscription) },
                    onOnlineDataBackupClick = {
                        navController.navigate(FCCScreen.DataBackup)
                    })

                FeedbackFeaturesSection(
                    modifier = Modifier,
                    onFeatureRequestClick = {
                        navController.navigate(
                            FCCScreen.FeatureRequest
                        )
                    },
                    onFeatureListClick = {
                        navController.navigate(
                            FCCScreen.FeatureRequestList
                        )
                    }
                )

                AppInformation()
            }



            when (screenState) {
                is ScreenState.Loading -> {
                    ScreenLoadingOverlay()
                }

                is ScreenState.Success -> {}

                is ScreenState.Error -> {
                    ErrorDialog {
                        settingsScreenCallbacks.resetScreenState()
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun FeedbackFeaturesSection(
    modifier: Modifier = Modifier,
    onFeatureRequestClick: () -> Unit = {},
    onFeatureListClick: () -> Unit = {},
) {
    Section(modifier) {
        SectionLabel(
            text = stringResource(id = R.string.feedback_features),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        NavigationListItem(
            title = stringResource(id = R.string.feature_request),
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.contact_support),
                    contentDescription = stringResource(id = R.string.feature_request)
                )
            }) {
            onFeatureRequestClick()
        }

        NavigationListItem(
            title = stringResource(id = R.string.feature_request_list),
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.list_alt_24),
                    contentDescription = stringResource(id = R.string.feature_request_list)
                )
            }) {
            onFeatureListClick()
        }
    }
}

@Composable
private fun AccountServicesSection(
    modifier: Modifier = Modifier,
    onPremiumClick: () -> Unit,
    onOnlineDataBackupClick: () -> Unit
) {
    Section(modifier) {
        SectionLabel(
            text = stringResource(id = R.string.account_services),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        NavigationListItem(
            title = stringResource(id = R.string.premium),
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.premium),
                    contentDescription = stringResource(id = R.string.premium)
                )
            }) {
            onPremiumClick()
        }

        NavigationListItem(
            title = stringResource(id = R.string.data_backup),
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.online),
                    contentDescription = stringResource(id = R.string.data_backup)
                )
            }) {
            onOnlineDataBackupClick()
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
private fun Defaults(
    settings: UserSettings?,
    currencies: Set<Currency>,
    saveButtonEnabled: Boolean,
    settingsScreenCallbacks: SettingsScreenCallbacks,
    modifier: Modifier = Modifier
) {
    Section(modifier) {
        SectionLabel(
            text = stringResource(id = R.string.your_defaults),
            Modifier.padding(bottom = 4.dp)
        )
        FCCTextField(
            title = stringResource(id = R.string.default_dish_tax),
            value = settings?.defaultTax ?: "",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        ) {
            settingsScreenCallbacks.updateDefaultTax(it)
        }

        FCCTextField(
            title = stringResource(id = R.string.default_dish_margin),
            value = settings?.defaultMargin ?: "",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        ) {
            settingsScreenCallbacks.updateDefaultMargin(it)
        }

        CheckBoxField(
            title = stringResource(id = R.string.use_metric_units),
            value = settings?.metricUsed == true,
            onValueChange = { settingsScreenCallbacks.updateMetricUsed(it) }
        )

        CheckBoxField(
            title = stringResource(id = R.string.use_us_units),
            value = settings?.imperialUsed == true,
            onValueChange = { settingsScreenCallbacks.updateImperialUsed(it) }
        )

        CheckBoxField(
            title = stringResource(id = R.string.use_half_products),
            value = settings?.showHalfProducts == true,
            onValueChange = { settingsScreenCallbacks.updateShowHalfProducts(it) }
        )

        CheckBoxField(
            title = stringResource(id = R.string.use_product_tax),
            value = settings?.showProductTax == true,
            onValueChange = { settingsScreenCallbacks.updateShowProductTax(it) }
        )

        Column {
            FieldLabel(text = stringResource(id = R.string.default_currency))
            Spacer(Modifier.size(2.dp))
            CurrenciesDropDown(
                currencies = currencies,
                selectedCurrency = settings?.currency,
                selectCurrency = { settingsScreenCallbacks.updateDefaultCurrencyCode(it) }
            )
        }

        ButtonRow(primaryButton = {
            FCCPrimaryButton(
                text = stringResource(id = R.string.save),
                enabled = saveButtonEnabled
            ) { settingsScreenCallbacks.saveSettings() }
        })
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
    selectedCurrency: Currency?,
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
                value = selectedCurrency?.displayName
                    ?: stringResource(R.string.currencies_failed_to_load),
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

@Preview(showBackground = true, heightDp = 1160)
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
                imperialUsed = false,
                showHalfProducts = true,
                showProductTax = true
            ),
            currencies = Currency.getAvailableCurrencies(),
            saveButtonEnabled = true,
            screenState = ScreenState.Idle,
            settingsScreenCallbacks = SettingsScreenCallbacks()
        )
    }
}


@Preview
@Composable
private fun PreviewDefaults() {
    FCCTheme {
        Defaults(
            settings = UserSettings(
                defaultMargin = "10",
                defaultTax = "10",
                currency = Currency.getInstance("USD"),
                metricUsed = true,
                imperialUsed = false,
                showHalfProducts = false,
                showProductTax = true
            ),
            currencies = Currency.getAvailableCurrencies(),
            saveButtonEnabled = true,
            settingsScreenCallbacks = SettingsScreenCallbacks()
        )
    }
}