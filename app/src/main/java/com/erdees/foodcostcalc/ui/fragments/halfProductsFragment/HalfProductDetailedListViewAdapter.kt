package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.utils.UnitsUtils.getUnitAbbreviation
import com.erdees.foodcostcalc.utils.Utils.formatPriceOrWeight
import com.erdees.foodcostcalc.utils.Utils.getBasicRecipeAsPercentageOfTargetRecipe
import com.erdees.foodcostcalc.utils.Utils.getIngredientForHundredPercentOfRecipe
import com.erdees.foodcostcalc.utils.Utils.getPriceForHundredPercentOfRecipe

import java.text.NumberFormat

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class HalfProductDetailedListViewAdapter(
  private val context: Activity,
  private val productIncludedInHalfProductModelList: List<ProductIncludedInHalfProductModel>,
  private val quantity: Double,
  private val totalWeightOfMainRecipe: Double
) : ArrayAdapter<ProductIncludedInHalfProductModel>(
    context,
    R.layout.listview_dish_row,
    productIncludedInHalfProductModelList
) {

    lateinit var productNameText: TextView
    lateinit var productPriceText: TextView
    lateinit var productWeightText: TextView
    lateinit var productUnit: TextView

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.listview_dish_row, null, true)
        productNameText     = rowView.findViewById(R.id.product_name_in_dish_row)
        productWeightText   = rowView.findViewById(R.id.product_weight_in_dish_row)
        productPriceText    = rowView.findViewById(R.id.product_price_in_dish_row)
        productUnit         = rowView.findViewById(R.id.product_weight_unit_in_dish_row)
        val quantityPercent =
            getBasicRecipeAsPercentageOfTargetRecipe(quantity, totalWeightOfMainRecipe)
        val weightIncludedQuantity = getIngredientForHundredPercentOfRecipe(
            productIncludedInHalfProductModelList[position].weight,
            quantityPercent
        )
        val formattedWeight = formatPriceOrWeight(weightIncludedQuantity)
        val priceIncludedQuantity = getPriceForHundredPercentOfRecipe(
            productIncludedInHalfProductModelList[position].totalPriceOfThisProduct,
            quantityPercent
        )
        val formatedPrice = NumberFormat.getCurrencyInstance().format(priceIncludedQuantity)
        setRowTextViews(position,formattedWeight,formatedPrice)
        setRowAsClickListener(rowView,position)
        return rowView
    }

    private fun setRowAsClickListener(rowView : View, position: Int){
        rowView.setOnClickListener {
            if (productIncludedInHalfProductModelList[position].weightUnit == "piece") {
                Toast.makeText(context, totalWeightMessage(position), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(
                context,
                productIncludedInHalfProductModelList[position].productModelIncluded.name,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setRowTextViews(position: Int, formattedWeight : String, formatedPrice : String){
        productNameText.text =
            productIncludedInHalfProductModelList[position].productModelIncluded.name
        productWeightText.text = formattedWeight
        productPriceText.text = formatedPrice
        productUnit.text =
            getUnitAbbreviation(productIncludedInHalfProductModelList[position].weightUnit)
    }

    private fun totalWeightMessage(position: Int):String {
        val isWeight =
            productIncludedInHalfProductModelList[position].halfProductModel.halfProductUnit == "per kilogram" ||
                    productIncludedInHalfProductModelList[position].halfProductModel.halfProductUnit == "per pound"
        val unitType: String = if (isWeight) " of weight " else " of volume "
        return productIncludedInHalfProductModelList[position].productModelIncluded.name +
                unitType +
                productIncludedInHalfProductModelList[position].totalWeightForPiece.toString() + " " +
                getUnitAbbreviation(
                    productIncludedInHalfProductModelList[position].halfProductModel.halfProductUnit.drop(
                        4
                    )
                ) + "."
    }
}
