package com.erdees.foodcostcalc.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.domain.model.units.UnitCategory

object UnitsUtils {

    @Composable
    fun getPerUnitAbbreviation(unit: MeasurementUnit): String {
        return when (unit) {
            MeasurementUnit.PIECE -> stringResource(R.string.abbreviation_per_piece)
            MeasurementUnit.KILOGRAM -> stringResource(R.string.abbreviation_per_kilogram)
            MeasurementUnit.GRAM -> stringResource(R.string.abbreviation_per_gram)
            MeasurementUnit.POUND -> stringResource(R.string.abbreviation_per_pound)
            MeasurementUnit.OUNCE -> stringResource(R.string.abbreviation_per_ounce)
            MeasurementUnit.LITER -> stringResource(R.string.abbreviation_per_liter)
            MeasurementUnit.MILLILITER -> stringResource(R.string.abbreviation_per_milliliter)
            MeasurementUnit.GALLON -> stringResource(R.string.abbreviation_per_gallon)
            MeasurementUnit.FLUID_OUNCE -> stringResource(R.string.abbreviation_default)
        }
    }

    @Composable
    fun getUnitAbbreviation(unit: MeasurementUnit): String {
        return stringResource(unit.symbolRes)
    }

    @Composable
    fun getUnitDisplayName(unit: MeasurementUnit): String {
        return stringResource(unit.displayNameRes)
    }

    @Composable
    fun getPerUnitAsDescription(unit: MeasurementUnit): String {
        return when (unit) {
            MeasurementUnit.KILOGRAM -> stringResource(R.string.description_per_kilogram)
            MeasurementUnit.GRAM -> stringResource(R.string.description_per_gram)
            MeasurementUnit.POUND -> stringResource(R.string.description_per_pound)
            MeasurementUnit.OUNCE -> stringResource(R.string.description_per_ounce)
            MeasurementUnit.LITER -> stringResource(R.string.description_per_liter)
            MeasurementUnit.MILLILITER -> stringResource(R.string.description_per_milliliter)
            MeasurementUnit.GALLON -> stringResource(R.string.description_per_gallon)
            else -> stringResource(R.string.description_default)
        }
    }

    @Composable
    fun getCategoryDisplayName(category: UnitCategory): String {
        return stringResource(category.displayNameRes)
    }

    // Simplified - now uses enum categories directly
    fun getUnitType(unit: MeasurementUnit?): UnitCategory? {
        return unit?.category
    }

    /**
     * Calculates the price of an item using the new enum-based unit system with built-in conversions
     */
    fun calculatePrice(
        pricePerUnit: Double,
        quantity: Double,
        itemUnit: MeasurementUnit,
        targetUnit: MeasurementUnit
    ): Double {
        return when (itemUnit.category) {
            UnitCategory.COUNT -> pricePerUnit * quantity
            else -> {
                // Use built-in enum conversion if units are compatible
                val convertedQuantity = itemUnit.convertTo(targetUnit, quantity) ?: quantity
                pricePerUnit * convertedQuantity
            }
        }
    }

    // Legacy string-based methods for backward compatibility during migration
    @Deprecated("Use MeasurementUnit enum version instead")
    @Composable
    fun getPerUnitAbbreviation(unit: String): String {
        val enumUnit = MeasurementUnit.fromStringOrDefault(unit.removePrefix("per "))
        return getPerUnitAbbreviation(enumUnit)
    }

    @Deprecated("Use MeasurementUnit enum version instead")
    @Composable
    fun getUnitAbbreviation(unit: String): String {
        val enumUnit = MeasurementUnit.fromStringOrDefault(unit)
        return getUnitAbbreviation(enumUnit)
    }

    @Deprecated("Use MeasurementUnit enum version instead")
    @Composable
    fun getPerUnitAsDescription(unit: String): String {
        val enumUnit = MeasurementUnit.fromStringOrDefault(unit.removePrefix("per "))
        return getPerUnitAsDescription(enumUnit)
    }

    @Deprecated("Use UnitCategory enum instead")
    enum class UnitType {
        WEIGHT,
        VOLUME,
        PIECE
    }

    @Deprecated("Use MeasurementUnit.category instead")
    fun getUnitType(string: String?): UnitType? {
        if (string == null) return null
        val enumUnit = MeasurementUnit.fromStringOrDefault(string.removePrefix("per "))
        return when (enumUnit.category) {
            UnitCategory.WEIGHT -> UnitType.WEIGHT
            UnitCategory.VOLUME -> UnitType.VOLUME
            UnitCategory.COUNT -> UnitType.PIECE
        }
    }

    @Deprecated("Use enum-based calculatePrice instead")
    fun calculatePrice(
        pricePerUnit: Double,
        quantity: Double,
        itemUnit: String,
        targetUnit: String
    ): Double {
        return if (itemUnit == "per piece") pricePerUnit * quantity
        else pricePerUnit * computeWeightAndVolumeToSameUnit(itemUnit, targetUnit, quantity)
    }

    // All the legacy conversion methods can be removed as they're now handled by MeasurementUnit.convertTo()
    @Deprecated("Use MeasurementUnit.convertTo() instead")
    fun computeWeightAndVolumeToSameUnit(
        finalUnit: String,
        anotherUnit: String,
        amount: Double
    ): Double {
        return when (finalUnit) {
            "per kilogram" -> computeUnitToKilogram(anotherUnit, amount)
            "per liter" -> computeUnitToLiter(anotherUnit, amount)
            "per pound" -> computeUnitToPound(anotherUnit, amount)
            else -> computeUnitToGallon(anotherUnit, amount)
        }
    }

    private fun computeUnitToKilogram(anotherUnit: String, amount: Double): Double {
        return when (anotherUnit) {
            "gram" -> amount / 1000
            "kilogram" -> amount
            "milliliter" -> amount / 1000
            "liter" -> amount
            "ounce" -> (amount / 1000) * 28.3495
            "pound" -> (amount / 1000) * 453.59237
            "fluid ounce" -> (amount / 1000) * 3785.41178 / 128
            "gallon" -> (amount / 1000) * 3785.41178
            else -> 900000.9
        }
    }

    private fun computeUnitToLiter(anotherUnit: String, amount: Double): Double {
        return when (anotherUnit) {
            "gram" -> amount / 1000
            "kilogram" -> amount
            "milliliter" -> amount / 1000
            "liter" -> amount
            "ounce" -> (amount / 1000) * 28.3495
            "pound" -> (amount / 1000) * 453.59237
            "fluid ounce" -> (amount / 1000) * 3785.41178 / 128
            "gallon" -> (amount / 1000) * 3785.41178
            else -> 900000.9
        }
    }

    private fun computeUnitToGallon(anotherUnit: String, amount: Double): Double {
        return when (anotherUnit) {
            "gram" -> amount * 0.264172052 / 1000
            "kilogram" -> amount * 0.264172052
            "milliliter" -> amount * 0.264172052 / 1000
            "liter" -> amount * 0.264172052
            "ounce" -> amount * 0.119826427 / 16
            "pound" -> amount * 0.119826427
            "fluid ounce" -> amount / 128
            "gallon" -> amount
            else -> 900000.9
        }
    }

    private fun computeUnitToPound(anotherUnit: String, amount: Double): Double {
        return when (anotherUnit) {
            "gram" -> amount / 453.59237
            "kilogram" -> amount * 2.20462262
            "milliliter" -> amount / 453.59237
            "liter" -> amount * 2.20462262
            "ounce" -> amount / 16
            "pound" -> amount
            "fluid ounce" -> amount * 8.345404436202464 / 128
            "gallon" -> amount * 8.345404436202464
            else -> 900000.9
        }
    }

}
