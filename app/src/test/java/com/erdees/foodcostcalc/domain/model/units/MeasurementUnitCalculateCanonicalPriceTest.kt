package com.erdees.foodcostcalc.domain.model.units

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.Test

class MeasurementUnitCalculateCanonicalPriceTest {

    @Test
    fun `calculates canonical price for 500g package at 2_50 euro`() {
        // Given: 500g package costing 2.50 euro
        val packagePrice = 2.50
        val packageQuantity = 500.0

        // When: calculating canonical price
        val (canonicalPrice, canonicalUnit) = MeasurementUnit.GRAM.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        // Then: should convert to price per kilogram
        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        canonicalPrice shouldBe 5.0.plusOrMinus(0.0001) // 2.50 / 0.5kg = 5.0 per kg
    }

    @Test
    fun `calculates canonical price for 100g package at 1_20 euro`() {
        val packagePrice = 1.20
        val packageQuantity = 100.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.GRAM.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        canonicalPrice shouldBe 12.0.plusOrMinus(0.0001) // 1.20 / 0.1kg = 12.0 per kg
    }

    @Test
    fun `calculates canonical price for 2kg package at 15_00 euro`() {
        val packagePrice = 15.00
        val packageQuantity = 2.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.KILOGRAM.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        canonicalPrice shouldBe 7.5.plusOrMinus(0.0001) // 15.00 / 2kg = 7.5 per kg
    }

    @Test
    fun `calculates canonical price for 0_5kg package at 3_75 euro`() {
        val packagePrice = 3.75
        val packageQuantity = 0.5

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.KILOGRAM.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        canonicalPrice shouldBe 7.5.plusOrMinus(0.0001) // 3.75 / 0.5kg = 7.5 per kg
    }

    @Test
    fun `calculates canonical price for 1 pound package at 4_53 euro`() {
        val packagePrice = 4.53
        val packageQuantity = 1.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.POUND.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        // 1 pound = 453.59237g, so price per kg should be: 4.53 / 0.45359237kg ≈ 9.99
        canonicalPrice shouldBe 9.986940476974954.plusOrMinus(0.0001)
    }

    @Test
    fun `calculates canonical price for 16 ounces package at 4_53 euro`() {
        val packagePrice = 4.53
        val packageQuantity = 16.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.OUNCE.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        // 16 ounces = 16 * 28.3495g = 453.592g ≈ 1 pound
        // So should be approximately same as 1 pound test above
        canonicalPrice shouldBe 9.986940476974954.plusOrMinus(0.0001)
    }

    @Test
    fun `calculates canonical price for 500ml package at 1_25 euro`() {
        val packagePrice = 1.25
        val packageQuantity = 500.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.MILLILITER.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.LITER
        canonicalPrice shouldBe 2.5.plusOrMinus(0.0001) // 1.25 / 0.5L = 2.5 per liter
    }

    @Test
    fun `calculates canonical price for 330ml package at 0_99 euro`() {
        val packagePrice = 0.99
        val packageQuantity = 330.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.MILLILITER.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.LITER
        canonicalPrice shouldBe 3.0.plusOrMinus(0.0001) // 0.99 / 0.33L = 3.0 per liter
    }

    @Test
    fun `calculates canonical price for 2L package at 3_50 euro`() {
        val packagePrice = 3.50
        val packageQuantity = 2.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.LITER.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.LITER
        canonicalPrice shouldBe 1.75.plusOrMinus(0.0001) // 3.50 / 2L = 1.75 per liter
    }

    @Test
    fun `calculates canonical price for 12 fl oz package at 1_50 euro`() {
        val packagePrice = 1.50
        val packageQuantity = 12.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.FLUID_OUNCE.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.LITER
        // 12 fl oz = 12 * 29.5735ml = 354.882ml = 0.354882L
        // Price per liter = 1.50 / 0.354882 ≈ 4.22675706
        canonicalPrice shouldBe 4.22675706.plusOrMinus(0.0001)
    }

    @Test
    fun `calculates canonical price for 1 gallon package at 12_00 euro`() {
        val packagePrice = 12.00
        val packageQuantity = 1.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.GALLON.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.LITER
        // 1 gallon = 3785.41178ml = 3.78541178L
        // Price per liter = 12.00 / 3.78541178 ≈ 3.170
        canonicalPrice shouldBe 3.1700622156404304.plusOrMinus(0.0001)
    }

    @Test
    fun `calculates canonical price for 6-pack at 4_50 euro`() {
        val packagePrice = 4.50
        val packageQuantity = 6.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.PIECE.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.PIECE
        canonicalPrice shouldBe 0.75.plusOrMinus(0.0001) // 4.50 / 6 pieces = 0.75 per piece
    }

    @Test
    fun `calculates canonical price for single item at 2_25 euro`() {
        val packagePrice = 2.25
        val packageQuantity = 1.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.PIECE.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.PIECE
        canonicalPrice shouldBe 2.25.plusOrMinus(0.0001) // 2.25 / 1 piece = 2.25 per piece
    }

    @Test
    fun `calculates canonical price for 24-pack at 18_00 euro`() {
        val packagePrice = 18.00
        val packageQuantity = 24.0

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.PIECE.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.PIECE
        canonicalPrice shouldBe 0.75.plusOrMinus(0.0001) // 18.00 / 24 pieces = 0.75 per piece
    }

    @Test
    fun `throws IllegalArgumentException when package quantity is zero`() {
        shouldThrow<IllegalArgumentException> {
            MeasurementUnit.GRAM.calculateCanonicalPrice(5.0, 0.0)
        }.message shouldBe "Package quantity cannot be zero."
    }

    @Test
    fun `throws IllegalArgumentException for all unit types when quantity is zero`() {
        val unitsToTest = listOf(
            MeasurementUnit.KILOGRAM,
            MeasurementUnit.LITER,
            MeasurementUnit.PIECE,
            MeasurementUnit.OUNCE,
            MeasurementUnit.MILLILITER
        )

        unitsToTest.forEach { unit ->
            shouldThrow<IllegalArgumentException> {
                unit.calculateCanonicalPrice(10.0, 0.0)
            }.message shouldBe "Package quantity cannot be zero."
        }
    }

    @Test
    fun `handles small package quantities correctly`() {
        val packagePrice = 0.01
        val packageQuantity = 1.0 // 1g

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.GRAM.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        canonicalPrice shouldBe 10.0.plusOrMinus(0.0001) // 0.01 / 0.000001kg = 10.0 per kg
    }

    @Test
    fun `handles very large package quantities correctly`() {
        val packagePrice = 1000.0
        val packageQuantity = 1000.0 // 1000kg

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.KILOGRAM.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        canonicalPrice shouldBe 1.0.plusOrMinus(0.0001) // 1000.0 / 1000kg = 1.0 per kg
    }

    @Test
    fun `handles fractional package quantities correctly`() {
        val packagePrice = 3.33
        val packageQuantity = 333.0 // 333g

        val (canonicalPrice, canonicalUnit) = MeasurementUnit.GRAM.calculateCanonicalPrice(
            packagePrice,
            packageQuantity
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        canonicalPrice shouldBe 9.999999999999999.plusOrMinus(0.0001) // ≈ 10.0 per kg
    }

    @Test
    fun `calculates price for 500g pasta package at 1_50 euro`() {
        val (canonicalPrice, canonicalUnit) = MeasurementUnit.GRAM.calculateCanonicalPrice(
            1.50,
            500.0
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        canonicalPrice shouldBe 3.0.plusOrMinus(0.0001)
    }

    @Test
    fun `calculates price for 1L milk bottle at 1_20 euro`() {
        val (canonicalPrice, canonicalUnit) = MeasurementUnit.LITER.calculateCanonicalPrice(
            1.20,
            1.0
        )

        canonicalUnit shouldBe MeasurementUnit.LITER
        canonicalPrice shouldBe 1.20.plusOrMinus(0.0001)
    }

    @Test
    fun `calculates price for 6-pack of eggs at 3_60 euro`() {
        val (canonicalPrice, canonicalUnit) = MeasurementUnit.PIECE.calculateCanonicalPrice(
            3.60,
            6.0
        )

        canonicalUnit shouldBe MeasurementUnit.PIECE
        canonicalPrice shouldBe 0.60.plusOrMinus(0.0001)
    }

    @Test
    fun `calculates price for 330ml beer can at 0_99 euro`() {
        val (canonicalPrice, canonicalUnit) = MeasurementUnit.MILLILITER.calculateCanonicalPrice(
            0.99,
            330.0
        )

        canonicalUnit shouldBe MeasurementUnit.LITER
        canonicalPrice shouldBe 3.0.plusOrMinus(0.0001)
    }

    @Test
    fun `calculates price for 10kg rice bag at 25_50 euro`() {
        val (canonicalPrice, canonicalUnit) = MeasurementUnit.KILOGRAM.calculateCanonicalPrice(
            25.50,
            10.0
        )

        canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        canonicalPrice shouldBe 2.55.plusOrMinus(0.0001)
    }

    @Test
    fun `calculates price for 5L cooking oil at 12_75 euro`() {
        val (canonicalPrice, canonicalUnit) = MeasurementUnit.LITER.calculateCanonicalPrice(
            12.75,
            5.0
        )

        canonicalUnit shouldBe MeasurementUnit.LITER
        canonicalPrice shouldBe 2.55.plusOrMinus(0.0001)
    }

    @Test
    fun `weight units always convert to KILOGRAM`() {
        val weightUnits = listOf(
            MeasurementUnit.GRAM,
            MeasurementUnit.KILOGRAM,
            MeasurementUnit.POUND,
            MeasurementUnit.OUNCE
        )

        weightUnits.forEach { unit ->
            val (_, canonicalUnit) = unit.calculateCanonicalPrice(10.0, 1.0)
            canonicalUnit shouldBe MeasurementUnit.KILOGRAM
        }
    }

    @Test
    fun `volume units always convert to LITER`() {
        val volumeUnits = listOf(
            MeasurementUnit.MILLILITER,
            MeasurementUnit.LITER,
            MeasurementUnit.FLUID_OUNCE,
            MeasurementUnit.GALLON
        )

        volumeUnits.forEach { unit ->
            val (_, canonicalUnit) = unit.calculateCanonicalPrice(10.0, 1.0)
            canonicalUnit shouldBe MeasurementUnit.LITER
        }
    }

    @Test
    fun `count units always convert to PIECE`() {
        val (_, canonicalUnit) = MeasurementUnit.PIECE.calculateCanonicalPrice(10.0, 1.0)
        canonicalUnit shouldBe MeasurementUnit.PIECE
    }
}
