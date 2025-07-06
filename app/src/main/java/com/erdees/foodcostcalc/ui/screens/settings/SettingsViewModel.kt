package com.erdees.foodcostcalc.ui.screens.settings

import android.icu.util.Currency
import androidx.core.bundle.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.settings.UserSettings
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsViewModel: ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()
    private val analyticsRepository: AnalyticsRepository by inject()

    private var _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val screenState = _screenState

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    @Suppress("MagicNumber")
    private suspend fun getSettingsModel(): UserSettings {
        return combine(
            preferences.defaultMargin,
            preferences.defaultTax,
            preferences.currency,
            preferences.metricUsed,
            preferences.imperialUsed,
            preferences.showHalfProducts,
            preferences.showProductTax,
        ) { array ->
            UserSettings(
                array[0] as String,
                array[1] as String,
                array[2] as Currency?,
                array[3] as Boolean,
                array[4] as Boolean,
                array[5] as Boolean,
                array[6] as Boolean,
            )
        }.first()
    }

    private fun getCurrencies(): Set<Currency> {
        val pattern = """\([^)]*\)"""
        return Currency
            .getAvailableCurrencies()
            .filter { !it.displayName.contains(Regex(pattern)) }
            .filter { !it.displayName.contains("Unknown") }
            .sortedBy { it.displayName }
            .toSet()
    }

    val currencies: StateFlow<Set<Currency>> = MutableStateFlow(getCurrencies()).asStateFlow()

    private var _settingsModel: MutableStateFlow<UserSettings?> = MutableStateFlow(null)
    val settingsModel = _settingsModel.onStart {
        _settingsModel.value = getSettingsModel()
    }.stateIn(viewModelScope,SharingStarted.Lazily,null)

    fun updateDefaultTax(newTax: String) {
        val tax =
            onNumericValueChange(oldValue = _settingsModel.value?.defaultTax ?: "", newValue = newTax)
        _settingsModel.value = _settingsModel.value?.copy(defaultTax = tax)
        analyticsRepository.logEvent(
            Constants.Analytics.Settings.TAX_CHANGED,
            Bundle().apply { putString(Constants.Analytics.Settings.TAX_VALUE, tax) })
    }

    fun updateDefaultMargin(newMargin: String) {
        val margin =
            onNumericValueChange(
                oldValue = _settingsModel.value?.defaultMargin ?: "",
                newValue = newMargin
            )
        _settingsModel.value = _settingsModel.value?.copy(defaultMargin = margin)
        analyticsRepository.logEvent(
            Constants.Analytics.Settings.MARGIN_CHANGED,
            Bundle().apply { putString(Constants.Analytics.Settings.MARGIN_VALUE, margin) })
    }

    fun updateDefaultCurrencyCode(currency: Currency) {
        _settingsModel.value = _settingsModel.value?.copy(currency = currency)
        analyticsRepository.logEvent(
            Constants.Analytics.Settings.CURRENCY_CHANGED,
            Bundle().apply { putString(Constants.Analytics.Settings.CURRENCY_CODE, currency.currencyCode) })
    }

    fun updateMetricUsed(newSetting: Boolean) {
        _settingsModel.value = _settingsModel.value?.copy(metricUsed = newSetting)
        analyticsRepository.logEvent(
            Constants.Analytics.Settings.METRIC_UNITS_USED_CHANGED,
            Bundle().apply { putBoolean(Constants.Analytics.Settings.IS_ENABLED, newSetting) })
    }

    fun updateImperialUsed(newSetting: Boolean) {
        _settingsModel.value = _settingsModel.value?.copy(imperialUsed = newSetting)
        analyticsRepository.logEvent(
            Constants.Analytics.Settings.IMPERIAL_UNITS_USED_CHANGED,
            Bundle().apply { putBoolean(Constants.Analytics.Settings.IS_ENABLED, newSetting) })
    }

    fun updateShowHalfProducts(newSetting: Boolean) {
        _settingsModel.value = _settingsModel.value?.copy(showHalfProducts = newSetting)
        analyticsRepository.logEvent(
            Constants.Analytics.Settings.SHOW_HALF_PRODUCTS_CHANGED,
            Bundle().apply { putBoolean(Constants.Analytics.Settings.IS_ENABLED, newSetting) })
    }

    fun updateShowProductTax(newSetting: Boolean) {
        _settingsModel.value = _settingsModel.value?.copy(showProductTax = newSetting)
        analyticsRepository.logEvent(
            Constants.Analytics.Settings.SHOW_PRODUCT_TAX_CHANGED,
            Bundle().apply { putBoolean(Constants.Analytics.Settings.IS_ENABLED, newSetting) })
    }

    /** Saves settings to preferences. */
    fun saveSettings() {
        _screenState.value = ScreenState.Loading<Nothing>()
        viewModelScope.launch {
            _settingsModel.value?.let { settingsModel ->
                preferences.setDefaultMargin(settingsModel.defaultMargin)
                preferences.setDefaultTax(settingsModel.defaultTax)
                settingsModel.currency?.currencyCode?.let {
                    preferences.setDefaultCurrencyCode(it)
                }
                preferences.setMetricUsed(settingsModel.metricUsed)
                preferences.setImperialUsed(settingsModel.imperialUsed)
                preferences.setShowHalfProducts(settingsModel.showHalfProducts)
                preferences.setShowProductTax(settingsModel.showProductTax)

                val bundle = Bundle().apply {
                    putString(Constants.Analytics.Settings.MARGIN_VALUE, settingsModel.defaultMargin)
                    putString(Constants.Analytics.Settings.TAX_VALUE, settingsModel.defaultTax)
                    putString(Constants.Analytics.Settings.CURRENCY_CODE, settingsModel.currency?.currencyCode)
                    putBoolean(Constants.Analytics.Settings.IS_ENABLED + "_metric", settingsModel.metricUsed)
                    putBoolean(Constants.Analytics.Settings.IS_ENABLED + "_imperial", settingsModel.imperialUsed)
                    putBoolean(Constants.Analytics.Settings.IS_ENABLED + "_half_products", settingsModel.showHalfProducts)
                    putBoolean(Constants.Analytics.Settings.IS_ENABLED + "_product_tax", settingsModel.showProductTax)
                }
                analyticsRepository.logEvent(Constants.Analytics.Settings.SAVED, bundle)
            }

        }
        _screenState.value = ScreenState.Success<Nothing>()
    }

    val saveButtonEnabled = _settingsModel.map {
        it?.defaultMargin?.toDoubleOrNull() != null &&
                it.defaultTax.toDoubleOrNull() != null &&
                atLeastOneUnitSelected()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private fun atLeastOneUnitSelected(): Boolean {
        return _settingsModel.value?.metricUsed == true || _settingsModel.value?.imperialUsed == true
    }
}
