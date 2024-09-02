package com.erdees.foodcostcalc.utils

import android.view.View
import android.widget.ListView


object ViewUtils {

    fun View.makeGone() {
        this.visibility = View.GONE
    }

    fun View.makeVisible() {
        this.visibility = View.VISIBLE
    }


    /**Computes height of listView based on each row height, includes dividers.*/
    fun getListSize(indicesOfBothLists: List<Int>, listView: ListView): Int {
        var result = 0
        for (eachProduct in indicesOfBothLists) {
            val listItem = listView.adapter.getView(eachProduct, null, listView)
            listItem.measure(0, View.MeasureSpec.UNSPECIFIED)
            result += listItem.measuredHeight
        }
        return result + (listView.dividerHeight * (listView.adapter.count - 1))
    }
}

