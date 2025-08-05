package com.erdees.foodcostcalc.domain.model.product

import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

sealed interface EditableProductDomain : Item {
    override val id: Long
    override val name: String
    val tax: String
    val waste: String
}

data class UnitPriceEditableProduct(
    override val id: Long,
    override val name: String,
    override val tax: String,
    override val waste: String,
    val unitPrice: String,
    val unitPriceUnit: MeasurementUnit
) : EditableProductDomain

data class PackagePriceEditableProduct(
    override val id: Long,
    override val name: String,
    override val tax: String,
    override val waste: String,
    val packagePrice: String,
    val packageQuantity: String,
    val packageUnit: MeasurementUnit
) : EditableProductDomain