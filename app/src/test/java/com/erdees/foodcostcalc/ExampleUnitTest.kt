package com.erdees.foodcostcalc

import org.junit.Test

import org.junit.Assert.*

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
        assertEquals(1.0, computeWeightToSameUnit("per liter","kilogram",1.0),0.01 )
        assertEquals(2.204, computeWeightToSameUnit("per liter","gram",2204.0),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per liter","pound",2.204),0.01 )
        assertEquals(5.66990463, computeWeightToSameUnit("per liter","ounce",200.0),0.01 )
        assertEquals(2.204, computeWeightToSameUnit("per liter","liter",2.204),0.01 )
        assertEquals(3.3330, computeWeightToSameUnit("per liter","milliliter",3333.0),0.01 )
        assertEquals(3.78541178, computeWeightToSameUnit("per liter","gallon",1.0),0.01 )
        assertEquals(3.78541178, computeWeightToSameUnit("per liter","fluid ounce",160.0),0.01 )
    }

    @Test
    fun computePoundIsCorrect() {
        assertEquals(2.204, computeWeightToSameUnit("per pound","kilogram",1.0),0.01 )
        assertEquals(0.00220462262, computeWeightToSameUnit("per pound","gram",1.0),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per pound","pound",1.0),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per pound","ounce",16.0),0.01 )
        assertEquals(2.204, computeWeightToSameUnit("per pound","liter",1.0),0.01 )
        assertEquals(2.204, computeWeightToSameUnit("per pound","milliliter",1000.0),0.01 )
        assertEquals(8.34537847, computeWeightToSameUnit("per pound","gallon",1.0),0.01 )
        assertEquals(0.05215877772, computeWeightToSameUnit("per pound","fluid ounce",1.0),0.01 )
        assertEquals(5.51155655, computeWeightToSameUnit("per pound","kilogram",2.5),0.01 )
        assertEquals(5.51155655, computeWeightToSameUnit("per pound","gram",2500.0),0.01 )
    }

    @Test
    fun computeKilogramIsCorrect(){
        assertEquals(1.0, computeWeightToSameUnit("per kilogram","kilogram",1.0),0.01 )
        assertEquals(2.204, computeWeightToSameUnit("per kilogram","gram",2204.0),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per kilogram","pound",2.204),0.01 )
        assertEquals(5.66990463, computeWeightToSameUnit("per kilogram","ounce",200.0),0.01 )
        assertEquals(2.204, computeWeightToSameUnit("per kilogram","liter",2.204),0.01 )
        assertEquals(3.3330, computeWeightToSameUnit("per kilogram","milliliter",3333.0),0.01 )
        assertEquals(3.78541178, computeWeightToSameUnit("per kilogram","gallon",1.0),0.01 )
        assertEquals(3.78541178, computeWeightToSameUnit("per kilogram","fluid ounce",160.0),0.01 )
    }

    @Test
    fun computeGallonIsCorrect(){
        assertEquals(1.0, computeWeightToSameUnit("per gallon","kilogram",3.78541178),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per gallon","gram",3785.41178),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per gallon","liter",3.78541178),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per gallon","milliliter",3785.41178),0.01 )
        assertEquals(0.264172052, computeWeightToSameUnit("per gallon","pound",2.204),0.01 )
        assertEquals(1.43791713, computeWeightToSameUnit("per gallon","pound",12.0),0.01 )
        assertEquals(0.0074891517 , computeWeightToSameUnit("per gallon","ounce",1.0),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per gallon","fluid ounce",160.0),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per gallon","gallon",1.0),0.01 )
        assertEquals(1.0, computeWeightToSameUnit("per gallon","pound",8.34537847),0.01 )

    }

}