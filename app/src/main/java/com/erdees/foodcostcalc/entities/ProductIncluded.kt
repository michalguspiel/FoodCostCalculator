package com.erdees.foodcostcalc.entities

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice


/**  Its a product but with dishOwnerId reference and weight */
@Keep
@Entity
data class ProductIncluded(
  @PrimaryKey(autoGenerate = true) val productIncludedId: Long,
  @Embedded val productIncluded: Product,
  val dishOwnerId: Long,
  @Embedded val dish: Dish, // Completely unused? Serves no purpose
  val productOwnerId: Long,
  var weight: Double,
  val weightUnit: String
) {


    @Ignore
    val totalPriceOfThisProduct: Double = calculatePrice(
        this.productIncluded.priceAfterWasteAndTax,
        this.weight,
        this.productIncluded.unit,
        this.weightUnit
    )

}





