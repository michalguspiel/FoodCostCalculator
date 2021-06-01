package com.erdees.foodcostcalc

import com.erdees.foodcostcalc.ads.AdHelper
import junit.framework.Assert.assertEquals
import org.junit.Test


class AdHelperTests {

    private val adCase = AdHelper(11,7)
    private val anotherAdCase = AdHelper(22,7)

    @Test
    fun `when theres 11 items in list there should be one ad, one theres 22 items on list there should be 3 ads added and thus item lists should be 12 and 25`(){
        assertEquals(1, adCase.howManyAds)
        assertEquals(3, anotherAdCase.howManyAds)
        assertEquals(12, adCase.newListSizeWithAds)
        assertEquals(25, anotherAdCase.newListSizeWithAds)
    }

    @Test
    fun `when there is 22 items and frequency 7 position of ads should return 6,13 and 20 `(){
        assertEquals(listOf(6,13,20),anotherAdCase.positionsOfAds())
    }

    @Test
    fun `Given itemsSize is 22 adFreq is 7 and position 9 is being binded in Recycler View, element 8 from the list should be binded`(){
        assertEquals(8,anotherAdCase.correctElementFromListToBind(9))
    }

    @Test
    fun `Given itemsize is 22 ad freq is 7 and pos 20 is being binded , element 17 from the list should be binded`(){
        assertEquals(17,anotherAdCase.correctElementFromListToBind(20))

    }


    @Test
    fun `given binding position should return correct amount of ads shown so far`(){
        assertEquals(1,anotherAdCase.adsBindedSoFar(9) )
        assertEquals(1,anotherAdCase.adsBindedSoFar(6) )
        assertEquals(1,anotherAdCase.adsBindedSoFar(7) )
        assertEquals(2,anotherAdCase.adsBindedSoFar(13) )
        assertEquals(2,anotherAdCase.adsBindedSoFar(19) )
        assertEquals(3,anotherAdCase.adsBindedSoFar(20) )
    }

}