package com.erdees.foodcostcalc.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.erdees.foodcostcalc.data.Preferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class Result {
  SUCCESS, FAILED_TAX_MISSING, FAILED_MARGIN_MISSING, FAILED_UNIT_SYSTEM_MISSING
}

class SettingsViewModel : ViewModel(), KoinComponent {

  private val preferences: Preferences by inject()
  fun getDefaultMargin(): String = preferences.defaultMargin
  fun getDefaultTax(): String = preferences.defaultTax
  fun getDefaultCurrency() = preferences.currency

  fun getIsMetricUsed(): Boolean = preferences.metricUsed
  fun getIsImperialUsed(): Boolean = preferences.imperialUsed

  private fun overrideDefaultMargin(newMargin: String) {
    preferences.defaultMargin = newMargin
  }

  private fun overrideDefaultTax(newTax: String) {
    preferences.defaultTax = newTax
  }

  private fun overrideDefaultCurrencyCode(newCode: String) {
    preferences.defaultCurrencyCode = newCode
  }

  private fun overrideIsMetricUsed(newSetting: Boolean) {
    preferences.metricUsed = newSetting
  }

  private fun overrideIsImperialUsed(newSetting: Boolean) {
    preferences.imperialUsed = newSetting
  }

  /** Saves settings, returns result string */
  fun saveSettings(
    margin: String,
    tax: String,
    isMetricChecked: Boolean,
    isImperialChecked: Boolean,
    currencyCode: String?
  ): Result {
    if (margin.isBlank() && margin != ".") {
      return Result.FAILED_MARGIN_MISSING
    }
    if (tax.isBlank() && tax != ".") {
      return Result.FAILED_TAX_MISSING
    }
    if (!isMetricChecked && !isImperialChecked) {
      return Result.FAILED_UNIT_SYSTEM_MISSING
    }

    overrideDefaultMargin(margin)
    overrideDefaultTax(tax)
    overrideIsMetricUsed(isMetricChecked)
    overrideIsImperialUsed(isImperialChecked)
    currencyCode?.let {
      overrideDefaultCurrencyCode(currencyCode)
    }
    return Result.SUCCESS
  }
}
