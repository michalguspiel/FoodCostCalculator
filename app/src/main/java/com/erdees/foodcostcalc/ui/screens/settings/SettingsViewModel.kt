package com.erdees.foodcostcalc.ui.screens.settings

import android.icu.util.Currency
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.settings.UserSettings
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()

    private var _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val screenState = _screenState

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    private fun getSettingsModel(): UserSettings {
        return UserSettings(
            preferences.defaultMargin,
            preferences.defaultTax,
            Currency.getInstance(preferences.defaultCurrencyCode),
            preferences.metricUsed,
            preferences.imperialUsed
        )
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

    private var _settingsModel = MutableStateFlow(getSettingsModel())
    val settingsModel = _settingsModel

    fun updateDefaultTax(newTax: String) {
        val tax =
            onNumericValueChange(oldValue = _settingsModel.value.defaultTax, newValue = newTax)
        _settingsModel.value = _settingsModel.value.copy(defaultTax = tax)
    }

    fun updateDefaultMargin(newMargin: String) {
        val margin =
            onNumericValueChange(
                oldValue = _settingsModel.value.defaultMargin,
                newValue = newMargin
            )
        _settingsModel.value = _settingsModel.value.copy(defaultMargin = margin)
    }

    fun updateDefaultCurrencyCode(currency: Currency) {
        _settingsModel.value = _settingsModel.value.copy(currency = currency)
    }

    fun updateMetricUsed(newSetting: Boolean) {
        _settingsModel.value = _settingsModel.value.copy(metricUsed = newSetting)
    }

    fun updateImperialUsed(newSetting: Boolean) {
        _settingsModel.value = _settingsModel.value.copy(imperialUsed = newSetting)
    }

    /** Saves settings to preferences. */
    fun saveSettings() {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch {
            preferences.defaultMargin = _settingsModel.value.defaultMargin
            preferences.defaultTax = _settingsModel.value.defaultTax
            preferences.defaultCurrencyCode = _settingsModel.value.currency.currencyCode
            preferences.metricUsed = _settingsModel.value.metricUsed
            preferences.imperialUsed = _settingsModel.value.imperialUsed
        }
        _screenState.value = ScreenState.Success
    }

    val saveButtonEnabled = _settingsModel.map {
        it.defaultMargin.toDoubleOrNull() != null &&
                it.defaultTax.toDoubleOrNull() != null &&
                atLeastOneUnitSelected()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private fun atLeastOneUnitSelected(): Boolean {
        return _settingsModel.value.metricUsed || _settingsModel.value.imperialUsed
    }
}
