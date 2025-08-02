package com.erdees.foodcostcalc.domain.model.halfProduct

import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.UnitsUtils
import kotlinx.serialization.Serializable

/**
 * Can be used in dish
 * */
@Keep
@Serializable
data class HalfProductAddedToDish(
    override val item: HalfProductDomain,
    override val quantity: Double,
    override val quantityUnit: MeasurementUnit,
) : ItemUsageEntry {
    override val foodCost = UnitsUtils.calculatePrice(
        item.pricePerUnit,
        quantity,
        item.halfProductUnit,
        quantityUnit
    )
}