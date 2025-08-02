package com.erdees.foodcostcalc.domain.model.product

import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

data class EditableProductDomain(
    override val id: Long,
    override val name: String,
    val pricePerUnit: String,
    val tax: String,
    val waste: String,
    val unit: MeasurementUnit
) : Item