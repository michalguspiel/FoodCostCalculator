package com.erdees.foodcostcalc.data

import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Currency
import com.erdees.foodcostcalc.utils.Constants
import java.util.Locale


interface Preferences {
  var defaultMargin: String
  var defaultTax: String
  var defaultCurrencyCode: String
  val currency: Currency?

  var isMetricUsed: Boolean
  var isImperialUsed: Boolean
}

class PreferencesImpl(context: Context) : Preferences {

  private val defaultLocale = Locale.getDefault()
  private val localeDefaultCurrencyCode = Currency.getInstance(defaultLocale).currencyCode

  private val sharedPreference = SharedPreferences(context)


  companion object {
    private var instance: Preferences? = null

    fun getInstance(context: Context): Preferences {
      val tempInstance = instance
      if (tempInstance != null) {
        return tempInstance
      } else {
        synchronized(this) {
          val instance = PreferencesImpl(context)
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
    set(value) {
      sharedPreference.save(Constants.METRIC, value)
    }
  override var isImperialUsed: Boolean
    get() = sharedPreference.getValueBoolean(Constants.IMPERIAL, false)
    set(value) {
      sharedPreference.save(Constants.IMPERIAL, value)
    }

  inner class SharedPreferences(val context: Context) {
    private val PREF_NAME = "settings"
    private val sharedPref: android.content.SharedPreferences =
      context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun save(KEY_NAME: String, text: String) {
      val editor: android.content.SharedPreferences.Editor = sharedPref.edit()
      editor.putString(KEY_NAME, text)
      editor.apply()
    }

    fun save(KEY_NAME: String, status: Boolean) {
      val editor: android.content.SharedPreferences.Editor = sharedPref.edit()
      editor.putBoolean(KEY_NAME, status)
      editor.apply()
    }

    fun getValueString(KEY_NAME: String): String? {
      return sharedPref.getString(KEY_NAME, null)
    }

    fun getValueBoolean(KEY_NAME: String, defaultValue: Boolean): Boolean {
      return sharedPref.getBoolean(KEY_NAME, defaultValue)
    }
  }
}
