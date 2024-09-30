package com.erdees.foodcostcalc.utils.ads

import android.util.Log
import com.erdees.foodcostcalc.domain.model.Ad
import com.erdees.foodcostcalc.domain.model.Item

class ListAdsInjectorManager(
    items: List<Item>,
    private val adFrequency: Int
) {

    private val totalAdCount = calculateInjectedAds(items.size, adFrequency).also {
        Log.i("ListAdsInjectorManager", "totalAdCount: $it")
    }


    private fun isAdPosition(index: Int): Boolean {
        return adPositions.contains(index)
    }

    /**
     *  List of positions where ads should be inserted.
     *
     *  Sequence of integers starting from `adFrequency` and incrementing by `adFrequency` for each subsequent position.
     *  It then takes the first `totalAdCount` elements from this sequence and converts them to a list.
     *
     *  Therefore the list with AdFrequency 3 will have ads at positions 3, 6, 9, 12, etc.
     */
    private val adPositions = generateSequence(adFrequency) { it + adFrequency }
        .take(totalAdCount)
        .toList()

    /**
     * Calculates the total number of ads to be injected into a list based on the list size and ad frequency.
     *
     * This function iteratively recalculates the number of ads needed until the total number of ads stabilizes.
     *
     * @param listSize The size of the original list of items.
     * @param adFrequency The frequency at which ads should be inserted.
     * @return The total number of ads to be injected.
     */

    private fun calculateInjectedAds(listSize: Int, adFrequency: Int): Int {
        if (adFrequency <= 0 || listSize <= 0) {
            return 0
        }

        var ls = listSize
        var adsCount = 0

        while (true) {
            val ads = ls / adFrequency
            if (ads == 0) break
            ls = 0
            adsCount += ads
            ls += ads
        }
        return adsCount
    }

    val listInjectedWithAds = (0 until items.size + totalAdCount).map { index ->
        if (isAdPosition(index)) {
            Ad
        } else {
            items[index - adPositions.count { it < index }]
        }
    }
}