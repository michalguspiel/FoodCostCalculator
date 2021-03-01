package com.example.foodcostcalc

import android.content.res.Resources
import androidx.room.Ignore
import com.example.foodcostcalc.fragments.dialogs.AddProductToDish
import com.example.foodcostcalc.viewmodel.AddViewModel
import java.text.DecimalFormat


fun formatPriceOrWeight(number: Double):String{
    val df = DecimalFormat("#.##")
    return df.format(number)
}




fun unitAbbreviation(unit: String): String =  when(unit){
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


/**Get units preferred by the user.*/
fun getUnits(resources: Resources, sharedPreferences: SharedPreferences): MutableList<String> {
    var chosenUnits = resources.getStringArray(R.array.piece)
    if (sharedPreferences.getValueBoolean("metric", true)) {
        chosenUnits += resources.getStringArray(R.array.addProductUnitsMetric)
    }
    if (sharedPreferences.getValueBoolean("usa", false)) {
        chosenUnits += resources.getStringArray(R.array.addProductUnitsUS)
    }
    return  chosenUnits.toMutableList()
}


/**Get chosen product and set correct type of units */
 fun setAdapterList(thisViewModel: AddViewModel, position: Int):String {

    return when (thisViewModel.readAllProductData.value?.get(position)?.unit) {
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
fun ArrayList<String>.changeUnitList(unitType: String,metricAsBoolean:Boolean,usaAsBoolean:Boolean) {
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
            this  += "piece"
            }
        }
    }

}