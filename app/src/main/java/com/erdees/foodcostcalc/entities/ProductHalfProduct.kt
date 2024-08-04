package com.erdees.foodcostcalc.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.erdees.foodcostcalc.utils.UnitsUtils
import kotlin.time.times

@Keep
@Entity(tableName = "Product_HalfProduct")
data class ProductHalfProduct(
  @PrimaryKey(autoGenerate = true) val productHalfProductId: Long,
  val productId: Long,
  val halfProductId: Long,
  val quantity: Double,
  val quantityUnit: String,
  val weightPiece: Double?
) {

  @Ignore
  val totalWeightForPiece = weightPiece?.let { quantity.times(it) }
}
