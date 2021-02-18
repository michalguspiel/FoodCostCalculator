package com.example.foodcostcalc.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.foodcostcalc.R
import com.example.foodcostcalc.model.ProductIncluded

class DishListViewAdapter(private val context: Activity, private val productsIncludedList: List<ProductIncluded>)
    : ArrayAdapter<ProductIncluded>(context, R.layout.listview_dish_row, productsIncludedList) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.listview_dish_row, null, true)

        val productNameText     = rowView.findViewById<TextView>(R.id.product_name_in_dish_row)
        val productWeightText   = rowView.findViewById<TextView>(R.id.product_weight_in_dish_row)
        val productPriceText    = rowView.findViewById<TextView>(R.id.product_price_in_dish_row)
        val productUnit         = rowView.findViewById<TextView>(R.id.product_weight_unit_in_dish_row)



        productNameText.text = productsIncludedList[position].productIncluded.name
        productWeightText.text = productsIncludedList[position].formattedWeightInCaseSomeoneIsCrazy
        productPriceText.text = productsIncludedList[position].finalFormatPriceOfProduct
        productUnit.text = productsIncludedList[position].unitAbbreviation
        return rowView
    }
}