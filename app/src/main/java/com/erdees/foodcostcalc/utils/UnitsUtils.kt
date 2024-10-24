package com.erdees.foodcostcalc.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.erdees.foodcostcalc.R

object UnitsUtils {

    @Composable
    fun getPerUnitAbbreviation(unit: String): String {
        return when (unit) {
            "per piece" -> stringResource(R.string.abbreviation_per_piece)
            "per kilogram" -> stringResource(R.string.abbreviation_per_kilogram)
            "per gram" -> stringResource(R.string.abbreviation_per_gram)
            "per pound" -> stringResource(R.string.abbreviation_per_pound)
            "per ounce" -> stringResource(R.string.abbreviation_per_ounce)
            "per liter" -> stringResource(R.string.abbreviation_per_liter)
            "per milliliter" -> stringResource(R.string.abbreviation_per_milliliter)
            "per gallon" -> stringResource(R.string.abbreviation_per_gallon)
            else -> stringResource(R.string.abbreviation_default)
        }
    }

    @Composable
    fun getUnitAbbreviation(unit: String): String {
        return when (unit) {
            "piece" -> stringResource(R.string.abbreviation_piece)
            "kilogram" -> stringResource(R.string.abbreviation_kilogram)
            "gram" -> stringResource(R.string.abbreviation_gram)
            "pound" -> stringResource(R.string.abbreviation_pound)
            "ounce" -> stringResource(R.string.abbreviation_ounce)
            "liter" -> stringResource(R.string.abbreviation_liter)
            "milliliter" -> stringResource(R.string.abbreviation_milliliter)
            "gallon" -> stringResource(R.string.abbreviation_gallon)
            else -> stringResource(R.string.abbreviation_default)
        }
    }

    @Composable
    fun getPerUnitAsDescription(unit: String): String {
        return when (unit) {
            "per kilogram" -> stringResource(R.string.description_per_kilogram)
            "per gram" -> stringResource(R.string.description_per_gram)
            "per pound" -> stringResource(R.string.description_per_pound)
            "per ounce" -> stringResource(R.string.description_per_ounce)
            "per liter" -> stringResource(R.string.description_per_liter)
            "per milliliter" -> stringResource(R.string.description_per_milliliter)
            "per gallon" -> stringResource(R.string.description_per_gallon)
            else -> stringResource(R.string.description_default)
        }
    }


    enum class UnitType {
        WEIGHT,
        VOLUME,
        PIECE
    }

    fun getUnitType(string: String?): UnitType? {
        return when (string) {
            "per kilogram", "per pound" -> {
                UnitType.WEIGHT
            }

            "per liter", "per gallon" -> {
                UnitType.VOLUME
            }

            "per piece" -> UnitType.PIECE

            else -> null
        }
    }

    /**
     * This function calculates the price of an item (product/half-product) inside a Dish or Half Product.
     *
     * @param pricePerUnit The price per unit of the product. This is a Double value.
     * @param quantity The amount of the product used. This is a Double value.
     * @param itemUnit The unit of measurement for the item.
     * @param targetUnit The unit of measurement of the chosen target.
     *
     * @return The calculated price of the product. This is a Double value.
     */
    fun calculatePrice(
        pricePerUnit: Double,
        quantity: Double,
        itemUnit: String,
        targetUnit: String
    ): Double {
        return if (itemUnit == "per piece") pricePerUnit * quantity
        else pricePerUnit * computeWeightAndVolumeToSameUnit(itemUnit, targetUnit, quantity)
    }

    /** Computes weight / volume to the same unit
     * approximate result for pure water of 4 degrees celsius.
     * */
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
