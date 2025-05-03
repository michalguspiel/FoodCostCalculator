package com.erdees.foodcostcalc.utils

import android.content.res.Resources
import com.erdees.foodcostcalc.R
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Utils {

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

    /**
     * Formats price to currency.
     *
     * This function is used in the whole project to get number formatted to currency.
     * @param number Double
     * @return String
     */
    fun formatPrice(number: Double, currency: android.icu.util.Currency?): String {
        val currencyCode = currency?.currencyCode
        val currency = currencyCode?.let { Currency.getInstance(it) }
        currencyCode?.let {
            getLocaleForCurrency(currencyCode)?.let { currencyLocale ->
                val format = NumberFormat.getCurrencyInstance(currencyLocale)
                format.currency = currency
                return format.format(number)
            }
        }
        val format = NumberFormat.getCurrencyInstance()
        currency?.let { format.currency = it }
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
            } catch (_: Exception) {
                // Ignore exceptions caused by unsupported locales
            }
        }
        return null
    }

    /**Get units preferred by the user.*/
    fun getUnitsSet(resources: Resources, isMetricUsed: Boolean, isImperialUsed: Boolean): Set<String> {
        var chosenUnits = resources.getStringArray(R.array.piece)
        if (isMetricUsed) {
            chosenUnits += resources.getStringArray(R.array.addProductUnitsMetric)
        }
        if (isImperialUsed) {
            chosenUnits += resources.getStringArray(R.array.addProductUnitsUS)
        }
        return chosenUnits.toSet()
    }


    // TODO, this should return list of enums which will be later on mapped to strings
    // TODO, otherwise this can't be fixed with string resources,
    // TODO, however, this also requires migrating database to use enums instead of strings
    /**Returns a set of units based on the unit type and user preferences.*/
    fun generateUnitSet(
        unitType: UnitsUtils.UnitType?,
        metricEnabled: Boolean,
        imperialEnabled: Boolean
    ): Set<String> {
        val units = mutableSetOf<String>()
        if (unitType == null) return units
        if (metricEnabled) {
            when (unitType) {
                UnitsUtils.UnitType.WEIGHT -> units += setOf("kilogram", "gram")
                UnitsUtils.UnitType.VOLUME -> units += setOf("milliliter", "liter")
                UnitsUtils.UnitType.PIECE -> units += "piece"
            }
        }
        if (imperialEnabled) {
            when (unitType) {
                UnitsUtils.UnitType.WEIGHT -> units += setOf("pound", "ounce")
                UnitsUtils.UnitType.VOLUME -> units += arrayListOf("gallon", "fluid ounce")
                UnitsUtils.UnitType.PIECE -> units += "piece"
            }
        }
        return units
    }
}