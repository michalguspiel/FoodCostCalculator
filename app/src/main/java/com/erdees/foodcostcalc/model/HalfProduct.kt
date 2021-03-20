package com.erdees.foodcostcalc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HalfProduct(
    @PrimaryKey(autoGenerate = true)val halfProductId: Long,
    val name: String,
    val halfProductUnit: String
    )
{
}