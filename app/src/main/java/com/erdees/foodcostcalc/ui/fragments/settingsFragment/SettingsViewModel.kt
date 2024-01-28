package com.erdees.foodcostcalc.ui.fragments.settingsFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.PreferencesDatabaseImpl

enum class Result {
  SUCCESS, FAILED_TAX_MISSING, FAILED_MARGIN_MISSING, FAILED_UNIT_SYSTEM_MISSING
}
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

  private val preferencesDatabase = PreferencesDatabaseImpl.getInstance(application)
  fun getDefaultMargin(): String = preferencesDatabase.defaultMargin
  fun getDefaultTax(): String = preferencesDatabase.defaultTax
  fun getDefaultCurrency() = preferencesDatabase.currency

  fun getIsMetricUsed(): Boolean = preferencesDatabase.isMetricUsed
  fun getIsImperialUsed(): Boolean = preferencesDatabase.isImperialUsed

  private fun overrideDefaultMargin(newMargin: String) {
    preferencesDatabase.defaultMargin = newMargin
  }

  private fun overrideDefaultTax(newTax: String) {
    preferencesDatabase.defaultTax = newTax
  }

  private fun overrideDefaultCurrencyCode(newCode: String) {
    preferencesDatabase.defaultCurrencyCode = newCode
  }

  private fun overrideIsMetricUsed(newSetting: Boolean) {
    preferencesDatabase.isMetricUsed = newSetting
  }

  private fun overrideIsImperialUsed(newSetting: Boolean) {
    preferencesDatabase.isImperialUsed = newSetting
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
