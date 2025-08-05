package com.erdees.foodcostcalc.ui.screens.products

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

sealed interface EditableProductUiState {
    val id: Long
    val name: String
    val tax: String
    val waste: String
}

data class UnitPriceState(
    override val id: Long = 0L,
    override val name: String = "",
    override val tax: String = "",
    override val waste: String = "",
    val unitPrice: String = "",
    val unitPriceUnit: MeasurementUnit = MeasurementUnit.KILOGRAM
) : EditableProductUiState

data class PackagePriceState(
    override val id: Long = 0L,
    override val name: String = "",
    override val tax: String = "",
    override val waste: String = "",
    val packagePrice: String = "",
    val packageQuantity: String = "",
    val packageUnit: MeasurementUnit = MeasurementUnit.KILOGRAM
) : EditableProductUiState