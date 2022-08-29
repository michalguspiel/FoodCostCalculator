package com.erdees.foodcostcalc.domain.model.product

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.erdees.foodcostcalc.domain.model.dish.DishModel
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice


/**  Its a product but with dishOwnerId reference and weight */
@Entity
data class ProductIncluded(
  @PrimaryKey(autoGenerate = true) val productIncludedId: Long,
  @Embedded val productModelIncluded: ProductModel,
  val dishOwnerId: Long,
  @Embedded val dishModel: DishModel,
  val productOwnerId: Long,
  var weight: Double,
  val weightUnit: String
) {


    @Ignore
    val totalPriceOfThisProduct: Double = calculatePrice(
        this.productModelIncluded.priceAfterWasteAndTax,
        this.weight,
        this.productModelIncluded.unit,
        this.weightUnit
    )

}





