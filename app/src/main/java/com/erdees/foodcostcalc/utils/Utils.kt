package com.erdees.foodcostcalc.utils

import android.content.Context
import android.content.res.Resources
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.data.PreferencesDatabaseImpl
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Utils {

  fun CharSequence.isNotBlankNorJustDot(): Boolean {
    return this.isNotBlank() && this.toString() != "."
  }

  /**Because some devices format number with commas which causes errors.*/
  fun formatResultAndCheckCommas(number: Double): String {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    val formattedResult = df.format(number)
    var formattedResultCheck = ""
    for (eachChar in formattedResult) {
      formattedResultCheck += if (eachChar == ',') '.'
      else eachChar
    }
    return formattedResultCheck
  }

  fun formatPriceOrWeight(number: Double): String {
    val df = DecimalFormat("#.##")
    return df.format(number)
  }

  /**
   * Formats price to currency.
   *
   * This function is used in the whole project to get number formatted to currency.
   * @param number Double
   * @return String
   */
  fun formatPrice(number: Double, context: Context): String {
    val preferencesDatabase = PreferencesDatabaseImpl.getInstance(context)
    val currencyCode = preferencesDatabase.currency?.currencyCode
    val currency = Currency.getInstance(currencyCode)
    currencyCode?.let {
        getLocaleForCurrency(currencyCode)?.let {currencyLocale ->
        val format = NumberFormat.getCurrencyInstance(currencyLocale)
        format.currency = currency
        return format.format(number)
      }
    }
    val format = NumberFormat.getCurrencyInstance()
    format.currency = currency
    return format.format(number)
  }

  private fun getLocaleForCurrency(currencyCode: String): Locale? {
    val availableLocales = Locale.getAvailableLocales()
    for (locale in availableLocales) {
      try {
        val currency = Currency.getInstance(locale)
        if (currency.currencyCode == currencyCode) {
          return locale
        }
      } catch (e: Exception) {
        // Ignore exceptions caused by unsupported locales
      }
    }
    return null
  }

  /**Get units preferred by the user.*/
  fun getUnits(resources: Resources, sharedPreferences: SharedPreferences): MutableList<String> {
    var chosenUnits = resources.getStringArray(R.array.piece)
    if (sharedPreferences.getValueBoolean("metric", true)) {
      chosenUnits += resources.getStringArray(R.array.addProductUnitsMetric)
    }
    if (sharedPreferences.getValueBoolean("usa", false)) {
      chosenUnits += resources.getStringArray(R.array.addProductUnitsUS)
    }
    return chosenUnits.toMutableList()
  }

  /**First clears unitList then adds correct units,
   *  every time data set changes this function is called.*/
  fun ArrayList<String>.changeUnitList(
    unitType: String,
    metricAsBoolean: Boolean,
    usaAsBoolean: Boolean
  ) {
    clear()
    if (metricAsBoolean) {
      when (unitType) {
        "weight" -> this += arrayListOf("kilogram", "gram")
        "volume" -> this += arrayListOf("milliliter", "liter")
        else -> {
          this.clear()
          this += "piece"
        }
      }
    }
    if (usaAsBoolean) {
      when (unitType) {
        "weight" -> this += arrayListOf("pound", "ounce")
        "volume" -> this += arrayListOf("gallon", "fluid ounce")
        else -> {
          clear()
          this += "piece"
        }
      }
    }
  }

  fun getBasicRecipeAsPercentageOfTargetRecipe(
    targetQuantity: Double,
    entryQuantity: Double
  ): Double {
    return entryQuantity * 100 / targetQuantity
  }

  fun getIngredientForHundredPercentOfRecipe(
    entryQuantity: Double,
    basicRecipeAsPercentOfFinalRecipe: Double
  ): Double {
    return entryQuantity * 100 / basicRecipeAsPercentOfFinalRecipe
  }

  fun getPriceForHundredPercentOfRecipe(entryPrice: Double, recipePercentage: Double): Double {
    return entryPrice * 100 / recipePercentage
  }

}
