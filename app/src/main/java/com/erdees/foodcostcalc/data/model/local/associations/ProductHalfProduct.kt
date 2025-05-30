package com.erdees.foodcostcalc.data.model.local.associations

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.erdees.foodcostcalc.data.model.local.HalfProductBase
import com.erdees.foodcostcalc.data.model.local.ProductBase

@Keep
@Entity(
  tableName = "Product_HalfProduct",
  indices = [Index("productId"), Index("halfProductId")],
  foreignKeys = [
    ForeignKey(
      entity = ProductBase::class,
      parentColumns = ["productId"],
      childColumns = ["productId"],
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity = HalfProductBase::class,
      parentColumns = ["halfProductId"],
      childColumns = ["halfProductId"],
      onDelete = ForeignKey.CASCADE
    )
  ]
)
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
