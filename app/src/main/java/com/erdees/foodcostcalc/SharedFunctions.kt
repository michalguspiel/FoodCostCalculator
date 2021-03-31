package com.erdees.foodcostcalc

import android.content.res.Resources
import java.text.DecimalFormat
import java.text.NumberFormat


fun formatPriceOrWeight(number: Double): String {
    val df = DecimalFormat("#.##")
    return df.format(number)
}

fun formatPrice(number: Double): String {
    return NumberFormat.getCurrencyInstance()
        .format(number)
}

fun MutableList<String>.filterWeight(): MutableList<String> {
    return   filter { it == "per kilogram" || it == "per pound" }.toMutableList()
}

fun MutableList<String>.filterVol(): MutableList<String> {
    return filter { it == "per liter" || it == "per gallon" }.toMutableList()
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

fun transformPerUnitToDescription(unit:String): String = when (unit){
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

/**Calculate product price inside dish or half product*/
fun calculatePrice(
    pricePerUnit: Double,
    amount: Double,
    productUnit: String,
    chosenUnit: String
): Double {
    return if (productUnit == "per piece") pricePerUnit * amount
    else pricePerUnit * computeWeightAndVolumeToSameUnit(productUnit,chosenUnit,amount)
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
        "per kilogram" -> computeUnitToKilogram(anotherUnit,amount)
        "per liter" -> computeUnitToLiter(anotherUnit,amount)
        "per pound" -> computeUnitToPound(anotherUnit,amount)
        else -> computeUnitToGallon(anotherUnit,amount)
    }
}

fun computeUnitToKilogram(anotherUnit: String,amount: Double) : Double {
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

fun computeUnitToLiter(anotherUnit: String,amount: Double) : Double{
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

fun computeUnitToGallon(anotherUnit: String, amount: Double) : Double{
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


