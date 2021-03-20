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


fun unitAbbreviation(unit: String): String = when (unit) {
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

fun perUnitToAbbreviation(unit:String): String = when (unit){
    "per kilogram" -> "weight as kilogram"
    "per gram" -> "weight as gram"
    "per pound" -> "weight as pound"
    "per ounce" -> "weight as ounce"
    "per liter" -> "volume as liter"
    "per milliliter" -> "volume as ml"
    "per gallon" -> "volume as gallon"
    else -> "fluid ounce"
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


fun calculatePrice(
    pricePerUnit: Double,
    weight: Double,
    productUnit: String,
    chosenUnit: String
): Double {
    return pricePerUnit *
            when (productUnit) {
                "per piece" -> weight
                "per kilogram" -> when (chosenUnit) {
                    "kilogram" -> weight
                    "gram" -> weight / 1000
                    "pound" -> (weight / 1000) * 453.59237
                    else -> (weight / 1000) * 28.3495
                }
                "per pound" -> when (chosenUnit) {
                    "kilogram" -> weight / 0.45359237
                    "gram" -> weight / 453.59237
                    "pound" -> weight
                    else -> weight / 16
                }
                "per gallon" -> when (chosenUnit) {
                    "liter" -> weight / 4.546092
                    "milliliter" -> weight / 4546.092
                    "gallon" -> weight
                    else -> weight / 128
                }
                "per liter" -> when (chosenUnit) {
                    "liter" -> weight
                    "milliliter" -> weight / 1000
                    "gallon" -> weight * 4.546092
                    else -> weight * 33.8140227
                }
                else -> weight
            }
}

/** Computes weight/ volume to the same unit
 * approximate result for pure water of 4 degrees celsius.
 * */
fun computeWeightToSameUnit(
    finalWeightUnit: String,
    thisWeightUnit: String,
    weight: Double
): Double {
    return when (finalWeightUnit) {
        "per kilogram" -> when (thisWeightUnit) {
            "gram" -> weight / 1000
            "kilogram" -> weight
            "milliliter" -> weight / 1000
            "liter" -> weight
            "ounce" -> (weight / 1000) * 28.3495
            "pound" -> (weight / 1000) * 453.59237
            "fluid ounce" -> (weight / 1000) * 29.5735295
            "gallon" -> (weight / 1000) * 3785.41178
            else -> 900000.9
        }
        "per liter" -> when (thisWeightUnit) {
            "gram" -> weight / 1000
            "kilogram" -> weight
            "milliliter" -> weight / 1000
            "liter" -> weight
            "ounce" -> (weight / 1000) * 28.3495
            "pound" -> (weight / 1000) * 453.59237
            "fluid ounce" -> (weight / 1000) * 29.5735295
            "gallon" -> (weight / 1000) * 3785.41178
            else -> 900000.9
        }
        "per pound" -> when (thisWeightUnit) {
            "gram" -> weight / 0.45359237
            "kilogram" -> weight / 453.59237
            "milliliter" -> weight / 0.45359237
            "liter" -> weight / 453.59237
            "ounce" -> weight / 16
            "pound" -> weight
            "fluid ounce" -> weight / 453.59237 / 4.546092 / 160
            "gallon" -> weight / 453.59237 / 4.546092
            else -> 900000.9
        }
        else -> when (thisWeightUnit) {
            "gram" -> weight * 4546.092
            "kilogram" -> weight * 4.546092
            "milliliter" -> weight * 4546.092
            "liter" -> weight * 4.546092
            "ounce" -> weight * 4.546092 * 2.20462262 * 16
            "pound" -> weight * 4.546092 * 2.20462262
            "fluid ounce" -> weight / 160
            "gallon" -> weight
            else -> 900000.9
        }


    }


}




