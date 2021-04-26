package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedFunctions.abbreviateUnit
import com.erdees.foodcostcalc.SharedFunctions.formatPriceOrWeight
import com.erdees.foodcostcalc.SharedFunctions.getBasicRecipeAsPercentageOfTargetRecipe
import com.erdees.foodcostcalc.SharedFunctions.getIngredientForHundredPercentOfRecipe
import com.erdees.foodcostcalc.SharedFunctions.getPriceForHundredPercentOfRecipe
import com.erdees.foodcostcalc.model.ProductIncludedInHalfProduct
import java.text.NumberFormat

class HalfProductListViewAdapter(private val context: Activity,
                                 private val productIncludedInHalfProductList: List<ProductIncludedInHalfProduct>,
                                 private val quantity : Double,
                                 private val totalWeightOfMainRecipe : Double)
    : ArrayAdapter<ProductIncludedInHalfProduct>(context, R.layout.listview_dish_row, productIncludedInHalfProductList) {

    lateinit var productNameText : TextView
    lateinit var productPriceText : TextView
    lateinit var productWeightText : TextView
    lateinit var productUnit : TextView

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.listview_dish_row, null, true)
        productNameText     = rowView.findViewById(R.id.product_name_in_dish_row)
        productWeightText   = rowView.findViewById(R.id.product_weight_in_dish_row)
        productPriceText    = rowView.findViewById(R.id.product_price_in_dish_row)
        productUnit         = rowView.findViewById(R.id.product_weight_unit_in_dish_row)
        val quantityPercent = getBasicRecipeAsPercentageOfTargetRecipe(quantity,totalWeightOfMainRecipe)
        val weightIncludedQuantity = getIngredientForHundredPercentOfRecipe(productIncludedInHalfProductList[position].weight,quantityPercent)
        val formattedWeight = formatPriceOrWeight(weightIncludedQuantity)
        val priceIncludedQuantity = getPriceForHundredPercentOfRecipe(productIncludedInHalfProductList[position].totalPriceOfThisProduct,quantityPercent)
        val formatedPrice = NumberFormat.getCurrencyInstance().format(priceIncludedQuantity)
        setRowTextViews(position,formattedWeight,formatedPrice)
        setRowAsClickListener(rowView,position)
        return rowView
    }

    private fun setRowAsClickListener(rowView : View, position: Int){
        rowView.setOnClickListener {
            if(productIncludedInHalfProductList[position].weightUnit == "piece") {
                Toast.makeText(context, totalWeightMessage(position),Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(context,productIncludedInHalfProductList[position].productIncluded.name,Toast.LENGTH_SHORT).show()
        }
    }

    private fun setRowTextViews(position: Int, formattedWeight : String, formatedPrice : String){
        productNameText.text = productIncludedInHalfProductList[position].productIncluded.name
        productWeightText.text = formattedWeight
        productPriceText.text = formatedPrice
        productUnit.text = abbreviateUnit(productIncludedInHalfProductList[position].weightUnit)
    }

    private fun totalWeightMessage(position: Int):String {
        val isWeight = productIncludedInHalfProductList[position].halfProduct.halfProductUnit == "per kilogram" ||
                productIncludedInHalfProductList[position].halfProduct.halfProductUnit == "per pound"
        val unitType: String = if(isWeight) " of weight " else " of volume "
        return productIncludedInHalfProductList[position].productIncluded.name +
                unitType +
                productIncludedInHalfProductList[position].totalWeightForPiece.toString() + " " +
                abbreviateUnit(productIncludedInHalfProductList[position].halfProduct.halfProductUnit.drop(4)) +"."
    }
}