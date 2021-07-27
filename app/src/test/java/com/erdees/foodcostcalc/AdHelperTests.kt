package com.erdees.foodcostcalc

import com.erdees.foodcostcalc.ads.AdHelper
import junit.framework.Assert.assertEquals
import org.junit.Test


class AdHelperTests {

    private val adCase = AdHelper(11,7)
    private val anotherAdCase = AdHelper(22,7)

    @Test
    fun `when theres 11 items in list there should be one ad, one theres 22 items on list there should be 3 ads added and thus item lists should be 13 and 28`(){
        assertEquals(1, adCase.finalAmountOfAds)
        assertEquals(3, anotherAdCase.finalAmountOfAds)
        assertEquals(12, adCase.initialListSize)
        assertEquals(25, anotherAdCase.initialListSize)
    }

    @Test
    fun `when there is 22 items and frequency 7 position of ads should return 6,13, 20 `(){
        assertEquals(listOf(7,14,21),anotherAdCase.positionsOfAds())
    }

    @Test
    fun `Given itemsSize is 22 adFreq is 7 and position 9 is being binded in Recycler View, element 8 from the list should be binded`(){
        assertEquals(8,anotherAdCase.correctElementFromListToBind(9))
    }

    @Test
    fun `Given itemsize is 22 ad freq is 7 and pos 20 is being binded , element 18 from the list should be binded`(){
        assertEquals(18,anotherAdCase.correctElementFromListToBind(20))

    }
    @Test
    fun `Given itemsize is 22 ad freq is 4 and pos 20 is being binded , element 15 from the list should be binded`(){
        val newAdCase = AdHelper(22,4)
        assertEquals(15,newAdCase.correctElementFromListToBind(20))
        assertEquals(16,newAdCase.correctElementFromListToBind(21))
        assertEquals(17,newAdCase.correctElementFromListToBind(22))
        assertEquals(18,newAdCase.correctElementFromListToBind(23))
        assertEquals(18,newAdCase.correctElementFromListToBind(24))// SAME CAUSE HERE AD IS BINDED
        assertEquals(19,newAdCase.correctElementFromListToBind(25))
        assertEquals(20,newAdCase.correctElementFromListToBind(26))

    }

    @Test
    fun `given binding position should return correct amount of ads shown so far`(){
        assertEquals(1,anotherAdCase.adsBindedSoFar(9) )
        assertEquals(0,anotherAdCase.adsBindedSoFar(6) )
        assertEquals(1,anotherAdCase.adsBindedSoFar(7) )
        assertEquals(1,anotherAdCase.adsBindedSoFar(13) )
        assertEquals(2,anotherAdCase.adsBindedSoFar(19) )
        assertEquals(2,anotherAdCase.adsBindedSoFar(20) )
    }


    @Test
    fun `given theres new adCase with frequency of 4 when theres 22 items in list there should be  6  ads and size should be 28`(){
        val newAdCase = AdHelper(22,4)
        assertEquals(1,newAdCase.adsBindedSoFar(4))
        assertEquals(2,newAdCase.adsBindedSoFar(8))
        assertEquals(3,newAdCase.adsBindedSoFar(12))
        assertEquals(4,newAdCase.adsBindedSoFar(16))
        assertEquals(5,newAdCase.adsBindedSoFar(20))
        assertEquals(5,newAdCase.adsBindedSoFar(23))
        assertEquals(6,newAdCase.adsBindedSoFar(27))
        assertEquals(6,newAdCase.finalAmountOfAds)
        assertEquals(28,newAdCase.finalListSize)
        assertEquals(21,newAdCase.correctElementFromListToBind(27))
        assertEquals(0,newAdCase.correctElementFromListToBind(0))
        assertEquals(1,newAdCase.correctElementFromListToBind(1))
        assertEquals(2,newAdCase.correctElementFromListToBind(2))
        assertEquals(3,newAdCase.correctElementFromListToBind(3))
        assertEquals(3,newAdCase.correctElementFromListToBind(4))
        assertEquals(4,newAdCase.correctElementFromListToBind(5))
    }


    @Test
    fun`Given i have list of size 13  and adFreq 3, amounts of ads should be 5 on pos 3,6,9,12,15 and the items should be binded correctly`() {
        val newTestCase = AdHelper(13,3)
        assertEquals(5,newTestCase.finalAmountOfAds)
        assertEquals(18,newTestCase.finalListSize)
        assertEquals(listOf(3, 6, 9, 12, 15),newTestCase.positionsOfAds())
        assertEquals(0,newTestCase.correctElementFromListToBind(0))
        assertEquals(1,newTestCase.correctElementFromListToBind(1))
        assertEquals(2,newTestCase.correctElementFromListToBind(2))
        assertEquals(2,newTestCase.correctElementFromListToBind(3))
        assertEquals(3,newTestCase.correctElementFromListToBind(4))
        assertEquals(4,newTestCase.correctElementFromListToBind(5))
        assertEquals(4,newTestCase.correctElementFromListToBind(6))
        assertEquals(5,newTestCase.correctElementFromListToBind(7))
        assertEquals(6,newTestCase.correctElementFromListToBind(8))
        assertEquals(6,newTestCase.correctElementFromListToBind(9))
        assertEquals(7,newTestCase.correctElementFromListToBind(10))
        assertEquals(8,newTestCase.correctElementFromListToBind(11))
        assertEquals(8,newTestCase.correctElementFromListToBind(12))
        assertEquals(9,newTestCase.correctElementFromListToBind(13))
        assertEquals(10,newTestCase.correctElementFromListToBind(14))
        assertEquals(10,newTestCase.correctElementFromListToBind(15))
        assertEquals(11,newTestCase.correctElementFromListToBind(16))
        assertEquals(12,newTestCase.correctElementFromListToBind(17))



    }

}