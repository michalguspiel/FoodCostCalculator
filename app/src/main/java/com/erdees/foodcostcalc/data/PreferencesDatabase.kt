package com.erdees.foodcostcalc.data

import android.content.Context
import android.icu.util.Currency
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants
import java.util.Locale


interface PreferencesDatabase {
  var defaultMargin: String
  var defaultTax: String
  var defaultCurrencyCode: String
  val currency: Currency?

  var isMetricUsed: Boolean
  var isImperialUsed: Boolean
}

class PreferencesDatabaseImpl(context: Context) : PreferencesDatabase {

  private val defaultLocale = Locale.getDefault()
  private val localeDefaultCurrencyCode = Currency.getInstance(defaultLocale).currencyCode

  private val sharedPreference = SharedPreferences(context)


  companion object {
    private var instance: PreferencesDatabase? = null

    fun getInstance(context: Context): PreferencesDatabase {
      val tempInstance = instance
      if (tempInstance != null) {
        return tempInstance
      } else {
        synchronized(this) {
          val instance = PreferencesDatabaseImpl(context)
          this.instance = instance
          return instance
        }
      }
    }
  }

  override var defaultMargin: String
    get() = sharedPreference.getValueString(Constants.MARGIN) ?: Constants.BASIC_MARGIN.toString()
    set(value) {
      sharedPreference.save(Constants.MARGIN, value)
    }
  override var defaultTax: String
    get() = sharedPreference.getValueString(Constants.TAX) ?: Constants.BASIC_TAX.toString()
    set(value) {
      sharedPreference.save(Constants.TAX, value)
    }
  override var defaultCurrencyCode: String
    get() = sharedPreference.getValueString(Constants.PREFERRED_CURRENCY_CODE)
      ?: localeDefaultCurrencyCode
    set(value) {
      sharedPreference.save(Constants.PREFERRED_CURRENCY_CODE, value)
    }
  override val currency: Currency?
    get() = Currency.getInstance(defaultCurrencyCode)
  override var isMetricUsed: Boolean
    get() = sharedPreference.getValueBoolean(Constants.METRIC, true)
    set(value) {sharedPreference.save(Constants.METRIC, value)}
  override var isImperialUsed: Boolean
    get() = sharedPreference.getValueBoolean(Constants.IMPERIAL, false)
    set(value) {sharedPreference.save(Constants.IMPERIAL, value)}
}
