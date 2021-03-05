package com.example.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.foodcostcalc.R
import com.example.foodcostcalc.calculatePrice
import com.example.foodcostcalc.formatPrice
import com.example.foodcostcalc.model.GrandDish
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.unitAbbreviation
import com.example.foodcostcalc.viewmodel.HalfProductsViewModel

class DishListViewAdapter(private val context: Activity,
                          private val grandDish: GrandDish,
                          private val halfProductsViewModel: HalfProductsViewModel,
                            private val viewLifecycleOwner: LifecycleOwner)
    : ArrayAdapter<Any>(context, R.layout.listview_dish_row,
    grandDish.productsIncluded
        + grandDish.halfProducts) {


    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.listview_dish_row,
            parent, false)

        val productNameText = rowView.findViewById<TextView>(R.id.product_name_in_dish_row)
        val productWeightText = rowView.findViewById<TextView>(R.id.product_weight_in_dish_row)
        val productPriceText = rowView.findViewById<TextView>(R.id.product_price_in_dish_row)
        val productUnit = rowView.findViewById<TextView>(R.id.product_weight_unit_in_dish_row)

        /**To populate rows with dishWithProduct products included */
        if (position < grandDish.productsIncluded.size ) {
            productNameText.text = grandDish.productsIncluded[position].productIncluded.name
            productWeightText.text = grandDish.productsIncluded[position].formattedWeight
            productPriceText.text = grandDish.productsIncluded[position].finalFormatPriceOfProduct
            productUnit.text = unitAbbreviation(grandDish.productsIncluded[position].weightUnit)
        }

        /**To populate rest with halfProducts*/
        else if (position >= grandDish.productsIncluded.size)  {
            var thisPosition = position - grandDish.productsIncluded.size // to start counting position from new list
            productNameText.text = grandDish.halfProducts[thisPosition].halfProduct.name
            productWeightText.text = grandDish.halfProducts[thisPosition].weight.toString()


            halfProductsViewModel
                .getCertainHalfProductWithProductsIncluded(grandDish.halfProducts[thisPosition].halfProductOwnerId)
                .observe(viewLifecycleOwner,
                    { productPriceText.text =
                        formatPrice(
                            calculatePrice(it.pricePerUnit(),grandDish.halfProducts[thisPosition].weight,
                                it.halfProduct.halfProductUnit, grandDish.halfProducts[thisPosition].unit)
                            )
                    })

            productUnit.text = unitAbbreviation(grandDish.halfProducts[thisPosition].unit)
        }
        return rowView

    }

    }