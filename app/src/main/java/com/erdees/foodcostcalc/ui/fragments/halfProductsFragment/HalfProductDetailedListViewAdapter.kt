package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.ListviewDishRowBinding
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.utils.UnitsUtils.getUnitAbbreviation
import com.erdees.foodcostcalc.utils.Utils.formatPriceOrWeight
import com.erdees.foodcostcalc.utils.Utils.getBasicRecipeAsPercentageOfTargetRecipe
import com.erdees.foodcostcalc.utils.Utils.getIngredientForHundredPercentOfRecipe
import com.erdees.foodcostcalc.utils.Utils.getPriceForHundredPercentOfRecipe
import java.text.NumberFormat

class HalfProductDetailedListViewAdapter(
    private val context: Activity,
    private val productIncludedInHalfProductList: List<ProductIncludedInHalfProduct>,
    private val quantity: Double,
    private val totalWeightOfMainRecipe: Double
) : ArrayAdapter<ProductIncludedInHalfProduct>(
    context,
    R.layout.listview_dish_row,
    productIncludedInHalfProductList
) {
    private var _binding : ListviewDishRowBinding? = null
    private val binding : ListviewDishRowBinding get() = _binding!!

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        _binding = ListviewDishRowBinding.inflate(inflater,parent,false)

        val quantityPercent =
            getBasicRecipeAsPercentageOfTargetRecipe(quantity, totalWeightOfMainRecipe)
        val weightIncludedQuantity = getIngredientForHundredPercentOfRecipe(
            productIncludedInHalfProductList[position].weight,
            quantityPercent
        )
        val formattedWeight = formatPriceOrWeight(weightIncludedQuantity)
        val priceIncludedQuantity = getPriceForHundredPercentOfRecipe(
            productIncludedInHalfProductList[position].totalPriceOfThisProduct,
            quantityPercent
        )
        val formatedPrice = NumberFormat.getCurrencyInstance().format(priceIncludedQuantity)
        setRowTextViews(position,formattedWeight,formatedPrice)
        setRowAsClickListener(binding.root,position)
        return binding.root
    }

    private fun setRowAsClickListener(rowView : View, position: Int){
        rowView.setOnClickListener {
            if (productIncludedInHalfProductList[position].weightUnit == "piece") {
                Toast.makeText(context, totalWeightMessage(position), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(
                context,
                productIncludedInHalfProductList[position].productModelIncluded.name,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setRowTextViews(position: Int, formattedWeight : String, formatedPrice : String){
        binding.productNameInDishRow.text =
            productIncludedInHalfProductList[position].productModelIncluded.name
        binding.productWeightInDishRow.text = formattedWeight
        binding.productPriceInDishRow.text = formatedPrice
        binding.productWeightUnitInDishRow.text =
            getUnitAbbreviation(productIncludedInHalfProductList[position].weightUnit)
    }

    private fun totalWeightMessage(position: Int):String {
        val isWeight =
            productIncludedInHalfProductList[position].halfProductModel.halfProductUnit == "per kilogram" ||
                    productIncludedInHalfProductList[position].halfProductModel.halfProductUnit == "per pound"
        val unitType: String = if (isWeight) " of weight " else " of volume "
        return productIncludedInHalfProductList[position].productModelIncluded.name +
                unitType +
                productIncludedInHalfProductList[position].totalWeightForPiece.toString() + " " +
                getUnitAbbreviation(
                    productIncludedInHalfProductList[position].halfProductModel.halfProductUnit.drop(
                        4
                    )
                ) + "."
    }
}
