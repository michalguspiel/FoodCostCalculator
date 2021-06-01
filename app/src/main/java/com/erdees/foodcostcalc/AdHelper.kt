package com.erdees.foodcostcalc

class AdHelper(val itemsSize: Int, private val adFrequency: Int) {

    val howManyAds = itemsSize / adFrequency

    val newListSizeWithAds = itemsSize + itemsSize / adFrequency

    fun adsBindedSoFar(position: Int): Int {
        return (position + 1 ) / adFrequency
    }


    fun positionsOfAds() : List<Int>{
        val positions = mutableListOf<Int>()
        var position = adFrequency - 1  // initially first position
      for (eachAd in 0 until howManyAds) {
          positions += position
          position += 7
      }
        return positions
    }

    fun correctElementFromListToBind(bindingPosition: Int) : Int {
        val howManyAdsBindedSoFar = adsBindedSoFar(bindingPosition)
        return bindingPosition - howManyAdsBindedSoFar
    }

}