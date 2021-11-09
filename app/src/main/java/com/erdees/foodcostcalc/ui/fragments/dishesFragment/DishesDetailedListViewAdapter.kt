package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.ListviewDishRowBinding
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.GrandDishModel
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice
import com.erdees.foodcostcalc.utils.UnitsUtils.getUnitAbbreviation
import com.erdees.foodcostcalc.utils.Utils.formatPrice
import com.erdees.foodcostcalc.utils.Utils.formatPriceOrWeight
import java.text.NumberFormat

class DishesDetailedListViewAdapter(
    private val context: Activity,
    private var grandDishModel: GrandDishModel,
    private val servings: Int,
    private val viewModel: DishListViewAdapterViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : ArrayAdapter<Any>(
    context, R.layout.listview_dish_row,
    grandDishModel.productsIncluded
            + grandDishModel.halfProducts
) {
    private var _binding: ListviewDishRowBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        _binding = ListviewDishRowBinding.inflate(inflater, parent, false)

        /**To populate rows with products included */
        if (position < grandDishModel.productsIncluded.size) setRowAsProduct(position)

        /**To populate rest with half products*/
        else if (position >= grandDishModel.productsIncluded.size) {
            val positionOfHalfProduct =
                position - grandDishModel.productsIncluded.size // to start counting position from new list
            setRowAsHalfProduct(positionOfHalfProduct)
        }
        return binding.root
    }

    private fun setHalfProductRowPrice(positionOfHalfProduct: Int, productPriceTextView: TextView) {
        viewModel.getCertainHalfProductWithProductsIncluded(grandDishModel.halfProducts[positionOfHalfProduct].halfProductOwnerId)
            .observe(
                viewLifecycleOwner,
                {
                    productPriceTextView.text = formatPrice(
                        calculatePrice(
                            it.pricePerUnit(),
                            grandDishModel.halfProducts[positionOfHalfProduct].weight,
                            it.halfProductModel.halfProductUnit,
                            grandDishModel.halfProducts[positionOfHalfProduct].unit
                        ) * servings
                    )
                })
    }

    private fun setRowAsHalfProduct(positionOfHalfProduct: Int) {
        binding.productNameInDishRow.text =
            grandDishModel.halfProducts[positionOfHalfProduct].halfProductModel.name
        binding.productWeightInDishRow.text =
            (grandDishModel.halfProducts[positionOfHalfProduct].weight * servings).toString()
        binding.productWeightUnitInDishRow.text =
            getUnitAbbreviation(grandDishModel.halfProducts[positionOfHalfProduct].unit)
        setHalfProductRowPrice(positionOfHalfProduct, binding.productPriceInDishRow)
    }

    private fun setRowAsProduct(position: Int) {
        binding.productNameInDishRow.text =
            grandDishModel.productsIncluded[position].productModelIncluded.name
        binding.productWeightInDishRow.text =
            formatPriceOrWeight(grandDishModel.productsIncluded[position].weight * servings)
        binding.productPriceInDishRow.text = NumberFormat.getCurrencyInstance()
            .format(grandDishModel.productsIncluded[position].totalPriceOfThisProduct * servings)
        binding.productWeightUnitInDishRow.text =
            getUnitAbbreviation(grandDishModel.productsIncluded[position].weightUnit)
    }
}