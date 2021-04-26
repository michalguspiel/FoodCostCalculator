package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.erdees.foodcostcalc.*
import com.erdees.foodcostcalc.SharedFunctions.abbreviateUnit
import com.erdees.foodcostcalc.SharedFunctions.calculatePrice
import com.erdees.foodcostcalc.SharedFunctions.formatPrice
import com.erdees.foodcostcalc.SharedFunctions.formatPriceOrWeight
import com.erdees.foodcostcalc.model.GrandDish
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.DishListViewAdapterViewModel
import java.text.NumberFormat

class DishListViewAdapter(private val context: Activity,
                          private val grandDish: GrandDish,
                          private val servings : Int,
                          private val viewModel: DishListViewAdapterViewModel,
                          private val viewLifecycleOwner: LifecycleOwner)
    : ArrayAdapter<Any>(context, R.layout.listview_dish_row,
    grandDish.productsIncluded
        + grandDish.halfProducts) {

    lateinit var productUnit : TextView
    lateinit var productWeightText : TextView
    lateinit var productPriceText : TextView
    lateinit var productNameText : TextView

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.listview_dish_row,
            parent, false)

         productNameText = rowView.findViewById(R.id.product_name_in_dish_row)
         productWeightText = rowView.findViewById(R.id.product_weight_in_dish_row)
         productPriceText = rowView.findViewById(R.id.product_price_in_dish_row)
         productUnit = rowView.findViewById(R.id.product_weight_unit_in_dish_row)

        /**To populate rows with dishWithProduct products included */
        if (position < grandDish.productsIncluded.size ) setRowAsProduct(position)

        /**To populate rest with halfProducts*/
        else if (position >= grandDish.productsIncluded.size)  {
            var positionOfHalfProduct = position - grandDish.productsIncluded.size // to start counting position from new list
            setRowAsHalfProduct(positionOfHalfProduct)
        }
        return rowView
    }

    private fun setHalfProductRowPrice(positionOfHalfProduct: Int){
        viewModel
            .getCertainHalfProductWithProductsIncluded(grandDish.halfProducts[positionOfHalfProduct].halfProductOwnerId)
            .observe(viewLifecycleOwner,
                { productPriceText.text =
                    formatPrice(
                        calculatePrice(it.pricePerUnit(),grandDish.halfProducts[positionOfHalfProduct].weight,
                            it.halfProduct.halfProductUnit, grandDish.halfProducts[positionOfHalfProduct].unit) * servings
                    )
                })
    }

    private fun setRowAsHalfProduct(positionOfHalfProduct: Int) {
        productNameText.text = grandDish.halfProducts[positionOfHalfProduct].halfProduct.name
        productWeightText.text = (grandDish.halfProducts[positionOfHalfProduct].weight * servings).toString()
        productUnit.text = abbreviateUnit(grandDish.halfProducts[positionOfHalfProduct].unit)
        setHalfProductRowPrice(positionOfHalfProduct)
    }

    private fun setRowAsProduct(position: Int){
        productNameText.text = grandDish.productsIncluded[position].productIncluded.name
        productWeightText.text = formatPriceOrWeight(grandDish.productsIncluded[position].weight * servings)
        productPriceText.text = NumberFormat.getCurrencyInstance().format(grandDish.productsIncluded[position].totalPriceOfThisProduct * servings)
        productUnit.text = abbreviateUnit(grandDish.productsIncluded[position].weightUnit)
    }


    }