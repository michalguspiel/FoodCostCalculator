package com.erdees.foodcostcalc.utils

import android.content.res.Resources
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

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

    fun formatPrice(number: Double): String {
        return NumberFormat.getCurrencyInstance()
            .format(number)
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