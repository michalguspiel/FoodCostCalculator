package com.erdees.foodcostcalc

import com.erdees.foodcostcalc.utils.SharedFunctions.calculatePrice
import com.erdees.foodcostcalc.utils.SharedFunctions.computeWeightAndVolumeToSameUnit
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



}