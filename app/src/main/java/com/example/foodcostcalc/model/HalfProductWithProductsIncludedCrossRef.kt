package com.example.foodcostcalc.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import java.text.NumberFormat

@Entity(primaryKeys = ["halfProductId", "productIncludedInHalfProductId"])
data class HalfProductWithProductsIncludedCrossRef(
    val halfProductId: Long,
    val productIncludedInHalfProductId: Long
) {

}