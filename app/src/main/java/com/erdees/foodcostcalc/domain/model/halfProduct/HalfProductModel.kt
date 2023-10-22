package com.erdees.foodcostcalc.domain.model.halfProduct

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HalfProduct")
data class HalfProductModel(
    @PrimaryKey(autoGenerate = true) val halfProductId: Long,
    val name: String,
    val halfProductUnit: String
) {
}
