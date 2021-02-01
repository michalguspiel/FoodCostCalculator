package com.example.foodcostcalc.model

import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
/** Data class provides a product. */
data class Product( val name : String, val pricePerUnit: Double, val tax: Double, val waste : Double){

    private val priceAfterWasteAndTax = pricePerUnit + pricePerUnit * (waste/100) + pricePerUnit * (tax/100)
    var properPrice = priceAfterWasteAndTax

    var unit = "grams"


    override fun toString(): String {
     return   name + " price netto per kg: " +
             pricePerUnit.toString() +
             "price per kg after calculating tax and waste: " +
             priceAfterWasteAndTax.toString()
    }
}