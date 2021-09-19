package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.GrandDishModel
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice
import com.erdees.foodcostcalc.utils.UnitsUtils.getUnitAbbreviation
import com.erdees.foodcostcalc.utils.Utils.formatPrice
import com.erdees.foodcostcalc.utils.Utils.formatPriceOrWeight
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.DishListViewAdapterViewModel
import java.text.NumberFormat

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */

class DishesDetailedListViewAdapter(
    private val context: Activity,
    private val grandDishModel: GrandDishModel,
    private val servings: Int,
    private val viewModel: DishListViewAdapterViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : ArrayAdapter<Any>(
    context, R.layout.listview_dish_row,
    grandDishModel.productsIncluded
            + grandDishModel.halfProductModels
) {

    private lateinit var productUnit: TextView
    private lateinit var productWeightText: TextView
    private lateinit var productPriceText: TextView
    private lateinit var productNameText: TextView

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(
            R.layout.listview_dish_row,
            parent, false
        )

        productNameText = rowView.findViewById(R.id.product_name_in_dish_row)
        productWeightText = rowView.findViewById(R.id.product_weight_in_dish_row)
        productPriceText = rowView.findViewById(R.id.product_price_in_dish_row)
        productUnit = rowView.findViewById(R.id.product_weight_unit_in_dish_row)

        /**To populate rows with dishWithProduct products included */
        if (position < grandDishModel.productsIncluded.size) setRowAsProduct(position)

        /**To populate rest with halfProductModels*/
        else if (position >= grandDishModel.productsIncluded.size) {
            val positionOfHalfProduct =
                position - grandDishModel.productsIncluded.size // to start counting position from new list
            setRowAsHalfProduct(positionOfHalfProduct)
        }
        return rowView
    }

    private fun setHalfProductRowPrice(positionOfHalfProduct: Int, productPriceTextView: TextView) {
        viewModel
            .getCertainHalfProductWithProductsIncluded(grandDishModel.halfProductModels[positionOfHalfProduct].halfProductOwnerId)
            .observe(
                viewLifecycleOwner,
                {
                    val result = formatPrice(
                        calculatePrice(
                            it.pricePerUnit(),
                            grandDishModel.halfProductModels[positionOfHalfProduct].weight,
                            it.halfProductModel.halfProductUnit,
                            grandDishModel.halfProductModels[positionOfHalfProduct].unit
                        ) * servings
                    )
                    productPriceTextView.text = result
                })
    }

    private fun setRowAsHalfProduct(positionOfHalfProduct: Int) {
        productNameText.text =
            grandDishModel.halfProductModels[positionOfHalfProduct].halfProductModel.name
        productWeightText.text =
            (grandDishModel.halfProductModels[positionOfHalfProduct].weight * servings).toString()
        productUnit.text =
            getUnitAbbreviation(grandDishModel.halfProductModels[positionOfHalfProduct].unit)
        setHalfProductRowPrice(positionOfHalfProduct, productPriceText)
    }

    private fun setRowAsProduct(position: Int) {
        productNameText.text = grandDishModel.productsIncluded[position].productModelIncluded.name
        productWeightText.text =
            formatPriceOrWeight(grandDishModel.productsIncluded[position].weight * servings)
        productPriceText.text = NumberFormat.getCurrencyInstance()
            .format(grandDishModel.productsIncluded[position].totalPriceOfThisProduct * servings)
        productUnit.text = getUnitAbbreviation(grandDishModel.productsIncluded[position].weightUnit)
    }


}