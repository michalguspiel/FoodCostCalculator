package com.example.foodcostcalc.model


import androidx.room.Entity
import androidx.room.PrimaryKey


/** Data class provides a product. */
@Entity(tableName = "products")
data class Product(@PrimaryKey(autoGenerate = true) val productId: Long,
                   val name: String,
                   val pricePerUnit: Double,
                   val tax: Double,
                   val waste: Double){
    override fun toString():String{
        val priceAfterWasteAndTax = pricePerUnit + pricePerUnit * (waste/100) + pricePerUnit * (tax/100)
        return "$name, price per unit netto:$pricePerUnit. Price per unit with foodcost: $priceAfterWasteAndTax"
    }
}



