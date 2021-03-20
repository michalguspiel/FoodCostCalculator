package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.model.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.unitAbbreviation

class HalfProductListViewAdapter(private val context: Activity, private val productIncludedInHalfProductList: List<ProductIncludedInHalfProduct>)
    : ArrayAdapter<ProductIncludedInHalfProduct>(context, R.layout.listview_dish_row, productIncludedInHalfProductList) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.listview_dish_row, null, true)

        val productNameText     = rowView.findViewById<TextView>(R.id.product_name_in_dish_row)
        val productWeightText   = rowView.findViewById<TextView>(R.id.product_weight_in_dish_row)
        val productPriceText    = rowView.findViewById<TextView>(R.id.product_price_in_dish_row)
        val productUnit         = rowView.findViewById<TextView>(R.id.product_weight_unit_in_dish_row)


        fun totalWeightMessage():String {
            val isWeight = productIncludedInHalfProductList[position].halfProduct.halfProductUnit == "per kilogram" ||
                    productIncludedInHalfProductList[position].halfProduct.halfProductUnit == "per pound"
            val unitType: String = if(isWeight) " of weight " else " of volume "
         return productIncludedInHalfProductList[position].productIncluded.name +
                 unitType +
                 productIncludedInHalfProductList[position].totalWeightForPiece.toString() + " " +
                         unitAbbreviation(productIncludedInHalfProductList[position].halfProduct.halfProductUnit.drop(4)) +"."
        }


        productNameText.text = productIncludedInHalfProductList[position].productIncluded.name
        productWeightText.text = productIncludedInHalfProductList[position].formattedWeight
        productPriceText.text = productIncludedInHalfProductList[position].finalFormatPriceOfProduct
        productUnit.text = unitAbbreviation(productIncludedInHalfProductList[position].weightUnit)



        rowView.setOnClickListener {
         if(productIncludedInHalfProductList[position].weightUnit == "piece") {
             Toast.makeText(context, totalWeightMessage(),Toast.LENGTH_SHORT).show()
         }
            else Toast.makeText(context,productIncludedInHalfProductList[position].productIncluded.name,Toast.LENGTH_SHORT).show()
        }

        return rowView
    }
}