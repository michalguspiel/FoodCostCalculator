package com.example.foodcostcalc.model


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.text.DecimalFormat


/** Data class provides a product. */
@Entity(tableName = "products")
data class Product(@PrimaryKey(autoGenerate = true) val productId: Long,
                   @ColumnInfo(name = "product_name") val name: String,
                   val pricePerUnit: Double,
                   val tax: Double,
                   val waste: Double,
                   val unit: String) {
    @Ignore val priceAfterWasteAndTax = pricePerUnit + pricePerUnit * (waste / 100) + pricePerUnit * (tax / 100)

    override fun toString(): String {
        val df = DecimalFormat("#.##")
        val formatedBruttoPrice = df.format(priceAfterWasteAndTax)
        return "$name, price $unit netto: $pricePerUnit.\nPrice $unit with foodcost: $formatedBruttoPrice."
    }
}



