package com.erdees.foodcostcalc.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ListView
import androidx.core.content.ContextCompat
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat


object SharedFunctions {

    fun EditText.isNotEmptyNorJustDot(): Boolean {
        return this.text.isNotEmpty() && this.text.toString() != "."
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

    fun BottomNavigationView.uncheckAllItems() {
        menu.setGroupCheckable(0, true, false)
        for (i in 0 until menu.size()) {
            menu.getItem(i).isChecked = false
        }
        menu.setGroupCheckable(0, true, true)
    }

    fun View.makeSnackBar(name: String, context: Context) {
        val snackBar =
            Snackbar.make(this, "$name created successfully!", Snackbar.LENGTH_SHORT)
        snackBar.setAction("Okay") { snackBar.dismiss() }
        snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.orange_500))
            .show()
    }

    fun View.hideKeyboard() {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE)as InputMethodManager?)?.hideSoftInputFromWindow(
            this.windowToken,
            0
        )
    }

    fun View.makeGone(){
        this.visibility = View.GONE
    }

    fun View.makeVisible(){
        this.visibility = View.VISIBLE
    }

    fun formatPriceOrWeight(number: Double): String {
        val df = DecimalFormat("#.##")
        return df.format(number)
    }

    fun formatPrice(number: Double): String {
        return NumberFormat.getCurrencyInstance()
            .format(number)
    }

    fun MutableList<String>.filterWeight(): MutableList<String> {
        return filter { it == "per kilogram" || it == "per pound" }.toMutableList()
    }

    fun MutableList<String>.filterVol(): MutableList<String> {
        return filter { it == "per liter" || it == "per gallon" }.toMutableList()
    }

    fun abbreviateUnitWithPer(unit: String): String = when (unit) {
        "per piece" -> "pce"
        "per kilogram" -> "kg"
        "per gram" -> "g"
        "per pound" -> "lb"
        "per ounce" -> "oz"
        "per liter" -> "l"
        "per milliliter" -> "ml"
        "per gallon" -> "gal"
        else -> "fl oz"
    }

    fun abbreviateUnit(unit: String): String = when (unit) {
        "piece" -> "pce"
        "kilogram" -> "kg"
        "gram" -> "g"
        "pound" -> "lb"
        "ounce" -> "oz"
        "liter" -> "l"
        "milliliter" -> "ml"
        "gallon" -> "gal"
        else -> "fl oz"
    }

    fun transformPerUnitToDescription(unit: String): String = when (unit) {
        "per kilogram" -> "weight as kilogram"
        "per gram" -> "weight as gram"
        "per pound" -> "weight as pound"
        "per ounce" -> "weight as ounce"
        "per liter" -> "volume as liter"
        "per milliliter" -> "volume as ml"
        "per gallon" -> "volume as gallon"
        else -> "volume as fl ounce"
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


    /**Takes product unit string and returns correct unit type, also as string. */
    fun setAdapterList(string: String?): String {
        return when (string) {
            "per kilogram", "per pound" -> {
                "weight"
            }
            "per liter", "per gallon" -> {
                "volume"
            }
            else -> {
                "piece"
            }
        }
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

    /**Calculate product price inside dishModel or half product*/
    fun calculatePrice(
        pricePerUnit: Double,
        amount: Double,
        productUnit: String,
        chosenUnit: String
    ): Double {
        return if (productUnit == "per piece") pricePerUnit * amount
        else pricePerUnit * computeWeightAndVolumeToSameUnit(productUnit, chosenUnit, amount)
    }

    /** Computes weight / volume to the same unit
     * approximate result for pure water of 4 degrees celsius.
     * */
    fun computeWeightAndVolumeToSameUnit(
        finalUnit: String,
        anotherUnit: String,
        amount: Double
    ): Double {
        return when (finalUnit) {
            "per kilogram" -> computeUnitToKilogram(anotherUnit, amount)
            "per liter" -> computeUnitToLiter(anotherUnit, amount)
            "per pound" -> computeUnitToPound(anotherUnit, amount)
            else -> computeUnitToGallon(anotherUnit, amount)
        }
    }

    fun computeUnitToKilogram(anotherUnit: String, amount: Double): Double {
        return when (anotherUnit) {
            "gram" -> amount / 1000
            "kilogram" -> amount
            "milliliter" -> amount / 1000
            "liter" -> amount
            "ounce" -> (amount / 1000) * 28.3495
            "pound" -> (amount / 1000) * 453.59237
            "fluid ounce" -> (amount / 1000) * 3785.41178 / 128
            "gallon" -> (amount / 1000) * 3785.41178
            else -> 900000.9
        }
    }

    fun computeUnitToLiter(anotherUnit: String, amount: Double): Double {
        return when (anotherUnit) {
            "gram" -> amount / 1000
            "kilogram" -> amount
            "milliliter" -> amount / 1000
            "liter" -> amount
            "ounce" -> (amount / 1000) * 28.3495
            "pound" -> (amount / 1000) * 453.59237
            "fluid ounce" -> (amount / 1000) * 3785.41178 / 128
            "gallon" -> (amount / 1000) * 3785.41178
            else -> 900000.9
        }
    }

    fun computeUnitToGallon(anotherUnit: String, amount: Double): Double {
        return when (anotherUnit) {
            "gram" -> amount * 0.264172052 / 1000
            "kilogram" -> amount * 0.264172052
            "milliliter" -> amount * 0.264172052 / 1000
            "liter" -> amount * 0.264172052
            "ounce" -> amount * 0.119826427 / 16
            "pound" -> amount * 0.119826427
            "fluid ounce" -> amount / 128
            "gallon" -> amount
            else -> 900000.9
        }
    }

    fun computeUnitToPound(anotherUnit: String, amount: Double): Double {
        return when (anotherUnit) {
            "gram" -> amount / 453.59237
            "kilogram" -> amount * 2.20462262
            "milliliter" -> amount / 453.59237
            "liter" -> amount * 2.20462262
            "ounce" -> amount / 16
            "pound" -> amount
            "fluid ounce" -> amount * 8.345404436202464 / 128
            "gallon" -> amount * 8.345404436202464
            else -> 900000.9
        }
    }

    /**Computes height of listView based on each row height, includes dividers.
     * I'm using this approach so listView size is set and doesn't need to be scrollable.
     *I know that I could have also used linear layout and just add programmatically each row view. But I have chosen this solutions as it works well.*/
    fun getListSize(indicesOfBothLists: List<Int>, listView: ListView): Int {
        var result = 0
        for (eachProduct in indicesOfBothLists) {
            val listItem = listView.adapter.getView(eachProduct, null, listView)
            listItem.measure(0, View.MeasureSpec.UNSPECIFIED)
            result += listItem.measuredHeight
        }
        return result + (listView.dividerHeight * (listView.adapter.count - 1))
    }

    fun getBasicRecipeAsPercentageOfTargetRecipe(targetQuantity: Double, entryQuantity: Double): Double{
        return entryQuantity * 100 / targetQuantity
    }

    fun getIngredientForHundredPercentOfRecipe(entryQuantity: Double, basicRecipeAsPercentOfFinalRecipe: Double): Double {
        return entryQuantity * 100 / basicRecipeAsPercentOfFinalRecipe
    }

    fun getPriceForHundredPercentOfRecipe(entryPrice : Double , recipePercentage : Double):Double {
        return entryPrice * 100 / recipePercentage
    }
}

