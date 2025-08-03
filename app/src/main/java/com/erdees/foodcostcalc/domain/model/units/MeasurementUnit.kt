package com.erdees.foodcostcalc.domain.model.units

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.erdees.foodcostcalc.R

/**
 * Unit system matching existing implementation - fully localized with string resources
 * Based on current units.xml and UnitsUtils.kt
 */
@Keep
enum class MeasurementUnit(
    @StringRes val displayNameRes: Int,
    @StringRes val symbolRes: Int,
    val category: UnitCategory,
    val baseUnitMultiplier: Double
) {
    // WEIGHT UNITS (base: gram)
    GRAM(R.string.unit_gram, R.string.symbol_gram, UnitCategory.WEIGHT, 1.0),
    KILOGRAM(R.string.unit_kilogram, R.string.symbol_kilogram, UnitCategory.WEIGHT, 1000.0),
    POUND(R.string.unit_pound, R.string.symbol_pound, UnitCategory.WEIGHT, 453.59237),
    OUNCE(R.string.unit_ounce, R.string.symbol_ounce, UnitCategory.WEIGHT, 28.3495),

    // VOLUME UNITS (base: milliliter)
    MILLILITER(R.string.unit_milliliter, R.string.symbol_milliliter, UnitCategory.VOLUME, 1.0),
    LITER(R.string.unit_liter, R.string.symbol_liter, UnitCategory.VOLUME, 1000.0),
    FLUID_OUNCE(R.string.unit_fluid_ounce, R.string.symbol_fluid_ounce, UnitCategory.VOLUME, 29.5735),
    GALLON(R.string.unit_gallon, R.string.symbol_gallon, UnitCategory.VOLUME, 3785.41178),

    // COUNT UNITS (base: piece)
    PIECE(R.string.unit_piece, R.string.symbol_piece, UnitCategory.COUNT, 1.0);

    /**
     * Convert this unit to another unit within the same category, or between weight and volume using water density
     */
    fun convertTo(targetUnit: MeasurementUnit, value: Double): Double? {
        return when {
            // Same category conversion (standard case)
            this.category == targetUnit.category -> {
                val baseValue = value * this.baseUnitMultiplier
                baseValue / targetUnit.baseUnitMultiplier
            }

            // Cross-category conversion: Weight ↔ Volume using water density (1kg = 1L at 4°C)
            (this.category == UnitCategory.WEIGHT && targetUnit.category == UnitCategory.VOLUME) -> {
                // Convert weight to grams, then to milliliters (1g = 1ml for water), then to target volume unit
                val grams = value * this.baseUnitMultiplier
                val milliliters = grams // 1g water = 1ml water at 4°C
                milliliters / targetUnit.baseUnitMultiplier
            }

            (this.category == UnitCategory.VOLUME && targetUnit.category == UnitCategory.WEIGHT) -> {
                // Convert volume to milliliters, then to grams (1ml = 1g for water), then to target weight unit
                val milliliters = value * this.baseUnitMultiplier
                val grams = milliliters // 1ml water = 1g water at 4°C
                grams / targetUnit.baseUnitMultiplier
            }

            // COUNT units cannot be converted to/from other categories
            else -> null
        }
    }

    /**
     * Legacy pricing equivalency calculation that matches the old computeWeightAndVolumeToSameUnit logic.
     * This calculates "how many target-unit-equivalent portions does this amount represent for pricing purposes"
     * rather than pure unit conversion.
     */
    fun computePricingEquivalent(targetUnit: MeasurementUnit, amount: Double): Double {
        return when (this) {
            KILOGRAM -> computeToKilogramEquivalent(targetUnit, amount)
            LITER -> computeToLiterEquivalent(targetUnit, amount)
            POUND -> computeToPoundEquivalent(targetUnit, amount)
            GALLON -> computeToGallonEquivalent(targetUnit, amount)
            else -> amount // Fallback for unsupported conversions
        }
    }

    private fun computeToKilogramEquivalent(targetUnit: MeasurementUnit, amount: Double): Double {
        return when (targetUnit) {
            GRAM -> amount / 1000.0
            KILOGRAM -> amount
            MILLILITER -> amount / 1000.0
            LITER -> amount
            OUNCE -> (amount / 1000.0) * 28.3495
            POUND -> (amount / 1000.0) * 453.59237
            FLUID_OUNCE -> (amount / 1000.0) * 3785.41178 / 128.0
            GALLON -> (amount / 1000.0) * 3785.41178
            else -> 900000.9 // Legacy fallback value
        }
    }

    private fun computeToLiterEquivalent(targetUnit: MeasurementUnit, amount: Double): Double {
        return when (targetUnit) {
            GRAM -> amount / 1000.0
            KILOGRAM -> amount
            MILLILITER -> amount / 1000.0
            LITER -> amount
            OUNCE -> (amount / 1000.0) * 28.3495
            POUND -> (amount / 1000.0) * 453.59237
            FLUID_OUNCE -> (amount / 1000.0) * 3785.41178 / 128.0
            GALLON -> (amount / 1000.0) * 3785.41178
            else -> 900000.9 // Legacy fallback value
        }
    }

    private fun computeToGallonEquivalent(targetUnit: MeasurementUnit, amount: Double): Double {
        return when (targetUnit) {
            GRAM -> amount * 0.264172052 / 1000.0
            KILOGRAM -> amount * 0.264172052
            MILLILITER -> amount * 0.264172052 / 1000.0
            LITER -> amount * 0.264172052
            OUNCE -> amount * 0.119826427 / 16.0
            POUND -> amount * 0.119826427
            FLUID_OUNCE -> amount / 128.0
            GALLON -> amount
            else -> 900000.9 // Legacy fallback value
        }
    }

    private fun computeToPoundEquivalent(targetUnit: MeasurementUnit, amount: Double): Double {
        return when (targetUnit) {
            GRAM -> amount / 453.59237
            KILOGRAM -> amount * 2.20462262
            MILLILITER -> amount / 453.59237
            LITER -> amount * 2.20462262
            OUNCE -> amount / 16.0
            POUND -> amount
            FLUID_OUNCE -> amount * 8.345404436202464 / 128.0
            GALLON -> amount * 8.345404436202464
            else -> 900000.9 // Legacy fallback value
        }
    }

    /**
     * Check if this unit is compatible (convertible) with another unit
     */
    fun isCompatibleWith(other: MeasurementUnit): Boolean {
        return this.category == other.category
    }

    companion object {
        /**
         * Get all units for a specific category
         */
        fun getUnitsForCategory(category: UnitCategory): List<MeasurementUnit> {
            return entries.filter { it.category == category }
        }

        /**
         * Safe lookup from string with fallback - handles your existing string formats
         */
        fun fromStringOrDefault(unitString: String, default: MeasurementUnit = GRAM): MeasurementUnit {
            val cleanUnit = unitString.removePrefix("per ").trim('"')
            return try {
                valueOf(cleanUnit.uppercase())
            } catch (_: IllegalArgumentException) {
                // Fallback to default if no match found
                default
            }
        }
    }
}

@Keep
enum class UnitCategory(@StringRes val displayNameRes: Int) {
    WEIGHT(R.string.unit_category_weight),
    VOLUME(R.string.unit_category_volume),
    COUNT(R.string.unit_category_count)
}
