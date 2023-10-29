package com.erdees.foodcostcalc.ui.fragments.settingsFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.PreferencesDatabaseImpl

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
  private fun overrideIsMetricUsed(newSetting: Boolean){
    preferencesDatabase.isMetricUsed = newSetting
  }
  private fun overrideIsImperialUsed(newSetting: Boolean){
    preferencesDatabase.isImperialUsed = newSetting
  }

  fun saveSettings(
    margin: String,
    tax: String,
    isMetricChecked: Boolean,
    isImperialChecked: Boolean,
    currencyCode: String?
  ) {
    overrideDefaultMargin(margin)
    overrideDefaultTax(tax)
    overrideIsMetricUsed(isMetricChecked)
    overrideIsImperialUsed(isImperialChecked)
    currencyCode?.let {
      overrideDefaultCurrencyCode(currencyCode)
    }
  }
}
