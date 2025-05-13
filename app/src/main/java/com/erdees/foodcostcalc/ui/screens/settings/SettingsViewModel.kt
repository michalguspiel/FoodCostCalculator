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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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

    private suspend fun getSettingsModel(): UserSettings {
        return combine(
            preferences.defaultMargin,
            preferences.defaultTax,
            preferences.currency,
            preferences.metricUsed,
            preferences.imperialUsed,
            preferences.showHalfProducts,
        ) { array ->
            UserSettings(
                array[0] as String,
                array[1] as String,
                array[2] as Currency?,
                array[3] as Boolean,
                array[4] as Boolean,
                array[5] as Boolean,
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
    }

    fun updateDefaultMargin(newMargin: String) {
        val margin =
            onNumericValueChange(
                oldValue = _settingsModel.value?.defaultMargin ?: "",
                newValue = newMargin
            )
        _settingsModel.value = _settingsModel.value?.copy(defaultMargin = margin)
    }

    fun updateDefaultCurrencyCode(currency: Currency) {
        _settingsModel.value = _settingsModel.value?.copy(currency = currency)
    }

    fun updateMetricUsed(newSetting: Boolean) {
        _settingsModel.value = _settingsModel.value?.copy(metricUsed = newSetting)
    }

    fun updateImperialUsed(newSetting: Boolean) {
        _settingsModel.value = _settingsModel.value?.copy(imperialUsed = newSetting)
    }

    fun updateShowHalfProducts(newSetting: Boolean) {
        _settingsModel.value = _settingsModel.value?.copy(showHalfProducts = newSetting)
    }

    /** Saves settings to preferences. */
    fun saveSettings() {
        _screenState.value = ScreenState.Loading()
        viewModelScope.launch {
            _settingsModel.value?.let {  settingsModel ->
                preferences.setDefaultMargin(settingsModel.defaultMargin)
                preferences.setDefaultTax(settingsModel.defaultTax)
                settingsModel.currency?.currencyCode?.let {
                    preferences.setDefaultCurrencyCode(it)
                }
                preferences.setMetricUsed(settingsModel.metricUsed)
                preferences.setImperialUsed(settingsModel.imperialUsed)
                preferences.setShowHalfProducts(settingsModel.showHalfProducts)
            }

        }
        _screenState.value = ScreenState.Success()
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
