package com.erdees.foodcostcalc

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice
import com.erdees.foodcostcalc.utils.UnitsUtils.computeWeightAndVolumeToSameUnit
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        assertEquals(3,1+1+1)
    }

    // Legacy tests - keep for backward compatibility
    @Test
    fun computeLiterIsCorrect(){
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per liter","kilogram",1.0),0.01 )
        assertEquals(2.204, computeWeightAndVolumeToSameUnit("per liter","gram",2204.0),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per liter","pound",2.204),0.01 )
        assertEquals(5.66990463, computeWeightAndVolumeToSameUnit("per liter","ounce",200.0),0.01 )
        assertEquals(2.204, computeWeightAndVolumeToSameUnit("per liter","liter",2.204),0.01 )
        assertEquals(3.3330, computeWeightAndVolumeToSameUnit("per liter","milliliter",3333.0),0.01 )
        assertEquals(3.78541178, computeWeightAndVolumeToSameUnit("per liter","gallon",1.0),0.01 )
        assertEquals(3.78541178, computeWeightAndVolumeToSameUnit("per liter","fluid ounce",128.0),0.01 )
    }

    @Test
    fun computePoundIsCorrect() {
        assertEquals(2.204, computeWeightAndVolumeToSameUnit("per pound","kilogram",1.0),0.01 )
        assertEquals(0.00220462262, computeWeightAndVolumeToSameUnit("per pound","gram",1.0),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per pound","pound",1.0),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per pound","ounce",16.0),0.01 )
        assertEquals(2.204, computeWeightAndVolumeToSameUnit("per pound","liter",1.0),0.01 )
        assertEquals(2.204, computeWeightAndVolumeToSameUnit("per pound","milliliter",1000.0),0.01 )
        assertEquals(8.34537847, computeWeightAndVolumeToSameUnit("per pound","gallon",1.0),0.01 )
        assertEquals(8.34537847/128, computeWeightAndVolumeToSameUnit("per pound","fluid ounce",1.0),0.01 )
        assertEquals(5.51155655, computeWeightAndVolumeToSameUnit("per pound","kilogram",2.5),0.01 )
        assertEquals(5.51155655, computeWeightAndVolumeToSameUnit("per pound","gram",2500.0),0.01 )
    }

    @Test
    fun computeKilogramIsCorrect(){
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per kilogram","kilogram",1.0),0.01 )
        assertEquals(2.204, computeWeightAndVolumeToSameUnit("per kilogram","gram",2204.0),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per kilogram","pound",2.204),0.01 )
        assertEquals(5.66990463, computeWeightAndVolumeToSameUnit("per kilogram","ounce",200.0),0.01 )
        assertEquals(2.204, computeWeightAndVolumeToSameUnit("per kilogram","liter",2.204),0.01 )
        assertEquals(3.3330, computeWeightAndVolumeToSameUnit("per kilogram","milliliter",3333.0),0.01 )
        assertEquals(3.78541178, computeWeightAndVolumeToSameUnit("per kilogram","gallon",1.0),0.01 )
        assertEquals(3.78541178, computeWeightAndVolumeToSameUnit("per kilogram","fluid ounce",128.0),0.01 )
    }

    @Test
    fun computeGallonIsCorrect(){
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per gallon","kilogram",3.78541178),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per gallon","gram",3785.41178),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per gallon","liter",3.78541178),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per gallon","milliliter",3785.41178),0.01 )
        assertEquals(0.264172052, computeWeightAndVolumeToSameUnit("per gallon","pound",2.204),0.01 )
        assertEquals(1.43791713, computeWeightAndVolumeToSameUnit("per gallon","pound",12.0),0.01 )
        assertEquals(0.0074891517 , computeWeightAndVolumeToSameUnit("per gallon","ounce",1.0),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per gallon","fluid ounce",128.0),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per gallon","gallon",1.0),0.01 )
        assertEquals(1.0, computeWeightAndVolumeToSameUnit("per gallon","pound",8.34537847),0.01 )
    }


    @Test
    fun calculatePriceToKilogramIsCorrect(){
        assertEquals(10.0, calculatePrice(10.0,1.0,"per kilogram","kilogram"),0.01)
        assertEquals(10.0, calculatePrice(10.0,1.0*2.204,"per kilogram","pound"),0.01)
        assertEquals(10.0, calculatePrice(10.0,1000.0,"per kilogram","gram"),0.01)
        assertEquals(10.0, calculatePrice(10.0,1.0*2.204*16,"per kilogram","ounce"),0.01)
    }

    @Test
    fun calculatePriceToPoundIsCorrect(){
        assertEquals(10.0*2.204, calculatePrice(10.0,1.0,"per pound","kilogram"),0.01)
        assertEquals(10.0*2.204/1000, calculatePrice(10.0,1.0,"per pound","gram"),0.01)
        assertEquals(10.0, calculatePrice(10.0,1.0,"per pound","pound"),0.01)
        assertEquals(10.0, calculatePrice(10.0,1.0*16,"per pound","ounce"),0.01)
    }

    @Test
    fun calculatePriceToLiterIsCorrect(){
        assertEquals(10.0, calculatePrice(10.0,1.0,"per liter","liter"),0.01)
        assertEquals(10.0, calculatePrice(10.0,1000.0,"per liter","milliliter"),0.01)
        assertEquals(10.0 * 3.78541178, calculatePrice(10.0,1.0,"per liter","gallon"),0.01)
        assertEquals(10.0 * 3.78541178 / 128, calculatePrice(10.0,1.0,"per liter","fluid ounce"),0.01)
    }

    @Test
    fun calculatePriceToGallonIsCorrect(){
        assertEquals(10.0, calculatePrice(10.0,3.78541178,"per gallon","liter"),0.01)
        assertEquals(10.0/1000, calculatePrice(10.0,3.78541178,"per gallon","milliliter"),0.01)
        assertEquals(10.0, calculatePrice(10.0,1.0,"per gallon","gallon"),0.01)
        assertEquals(10.0/128, calculatePrice(10.0,1.0,"per gallon","fluid ounce"),0.01)
    }

    // NEW TESTS: Testing MeasurementUnit.convertTo() function with same test values

    @Test
    fun convertToFromLiterIsCorrect(){
        // Same test values as computeLiterIsCorrect but using new convertTo function
        assertEquals(1.0, MeasurementUnit.LITER.convertTo(MeasurementUnit.KILOGRAM, 1.0)!!, 0.01)
        assertEquals(2.204, MeasurementUnit.LITER.convertTo(MeasurementUnit.GRAM, 2204.0)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.LITER.convertTo(MeasurementUnit.POUND, 2.204)!!, 0.01)
        assertEquals(5.66990463, MeasurementUnit.LITER.convertTo(MeasurementUnit.OUNCE, 200.0)!!, 0.01)
        assertEquals(2.204, MeasurementUnit.LITER.convertTo(MeasurementUnit.LITER, 2.204)!!, 0.01)
        assertEquals(3.3330, MeasurementUnit.LITER.convertTo(MeasurementUnit.MILLILITER, 3333.0)!!, 0.01)
        assertEquals(3.78541178, MeasurementUnit.LITER.convertTo(MeasurementUnit.GALLON, 1.0)!!, 0.01)
        assertEquals(3.78541178, MeasurementUnit.LITER.convertTo(MeasurementUnit.FLUID_OUNCE, 128.0)!!, 0.01)
    }

    @Test
    fun convertToFromPoundIsCorrect() {
        // Same test values as computePoundIsCorrect but using new convertTo function
        assertEquals(2.204, MeasurementUnit.POUND.convertTo(MeasurementUnit.KILOGRAM, 1.0)!!, 0.01)
        assertEquals(0.00220462262, MeasurementUnit.POUND.convertTo(MeasurementUnit.GRAM, 1.0)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.POUND.convertTo(MeasurementUnit.POUND, 1.0)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.POUND.convertTo(MeasurementUnit.OUNCE, 16.0)!!, 0.01)
        assertEquals(2.204, MeasurementUnit.POUND.convertTo(MeasurementUnit.LITER, 1.0)!!, 0.01)
        assertEquals(2.204, MeasurementUnit.POUND.convertTo(MeasurementUnit.MILLILITER, 1000.0)!!, 0.01)
        assertEquals(8.34537847, MeasurementUnit.POUND.convertTo(MeasurementUnit.GALLON, 1.0)!!, 0.01)
        assertEquals(8.34537847/128, MeasurementUnit.POUND.convertTo(MeasurementUnit.FLUID_OUNCE, 1.0)!!, 0.01)
        assertEquals(5.51155655, MeasurementUnit.POUND.convertTo(MeasurementUnit.KILOGRAM, 2.5)!!, 0.01)
        assertEquals(5.51155655, MeasurementUnit.POUND.convertTo(MeasurementUnit.GRAM, 2500.0)!!, 0.01)
    }

    @Test
    fun convertToFromKilogramIsCorrect(){
        // Same test values as computeKilogramIsCorrect but using new convertTo function
        assertEquals(1.0, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.KILOGRAM, 1.0)!!, 0.01)
        assertEquals(2.204, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.GRAM, 2204.0)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.POUND, 2.204)!!, 0.01)
        assertEquals(5.66990463, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.OUNCE, 200.0)!!, 0.01)
        assertEquals(2.204, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.LITER, 2.204)!!, 0.01)
        assertEquals(3.3330, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.MILLILITER, 3333.0)!!, 0.01)
        assertEquals(3.78541178, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.GALLON, 1.0)!!, 0.01)
        assertEquals(3.78541178, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.FLUID_OUNCE, 128.0)!!, 0.01)
    }

    @Test
    fun convertToFromGallonIsCorrect(){
        // Same test values as computeGallonIsCorrect but using new convertTo function
        assertEquals(1.0, MeasurementUnit.GALLON.convertTo(MeasurementUnit.KILOGRAM, 3.78541178)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.GALLON.convertTo(MeasurementUnit.GRAM, 3785.41178)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.GALLON.convertTo(MeasurementUnit.LITER, 3.78541178)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.GALLON.convertTo(MeasurementUnit.MILLILITER, 3785.41178)!!, 0.01)
        assertEquals(0.264172052, MeasurementUnit.GALLON.convertTo(MeasurementUnit.POUND, 2.204)!!, 0.01)
        assertEquals(1.43791713, MeasurementUnit.GALLON.convertTo(MeasurementUnit.POUND, 12.0)!!, 0.01)
        assertEquals(0.0074891517, MeasurementUnit.GALLON.convertTo(MeasurementUnit.OUNCE, 1.0)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.GALLON.convertTo(MeasurementUnit.FLUID_OUNCE, 128.0)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.GALLON.convertTo(MeasurementUnit.GALLON, 1.0)!!, 0.01)
        assertEquals(1.0, MeasurementUnit.GALLON.convertTo(MeasurementUnit.POUND, 8.34537847)!!, 0.01)
    }

    // Additional tests for same-category conversions (these should also work)
    @Test
    fun convertToSameCategoryIsCorrect() {
        // Weight conversions
        assertEquals(1000.0, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.GRAM, 1.0)!!, 0.01)
        assertEquals(2.20462262, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.POUND, 1.0)!!, 0.01)
        assertEquals(35.2739619, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.OUNCE, 1.0)!!, 0.01)

        // Volume conversions
        assertEquals(1000.0, MeasurementUnit.LITER.convertTo(MeasurementUnit.MILLILITER, 1.0)!!, 0.01)
        assertEquals(0.264172052, MeasurementUnit.LITER.convertTo(MeasurementUnit.GALLON, 1.0)!!, 0.01)
        assertEquals(33.8140227, MeasurementUnit.LITER.convertTo(MeasurementUnit.FLUID_OUNCE, 1.0)!!, 0.01)
    }

    // Test that incompatible conversions return null
    @Test
    fun convertToIncompatibleReturnsNull() {
        // COUNT units cannot convert to other categories
        assertEquals(null, MeasurementUnit.PIECE.convertTo(MeasurementUnit.KILOGRAM, 1.0))
        assertEquals(null, MeasurementUnit.PIECE.convertTo(MeasurementUnit.LITER, 1.0))
        assertEquals(null, MeasurementUnit.KILOGRAM.convertTo(MeasurementUnit.PIECE, 1.0))
        assertEquals(null, MeasurementUnit.LITER.convertTo(MeasurementUnit.PIECE, 1.0))
    }
}
