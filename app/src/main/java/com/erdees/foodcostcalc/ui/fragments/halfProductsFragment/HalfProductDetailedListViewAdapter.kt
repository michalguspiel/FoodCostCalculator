//package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment
//
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.Toast
//import com.erdees.foodcostcalc.R
//import com.erdees.foodcostcalc.databinding.ListviewDishRowBinding
//import com.erdees.foodcostcalc.utils.UnitsUtils.getUnitAbbreviation
//import com.erdees.foodcostcalc.utils.Utils
//import com.erdees.foodcostcalc.utils.Utils.formatPriceOrWeight
//import com.erdees.foodcostcalc.utils.Utils.getBasicRecipeAsPercentageOfTargetRecipe
//import com.erdees.foodcostcalc.utils.Utils.getIngredientForHundredPercentOfRecipe
//import com.erdees.foodcostcalc.utils.Utils.getPriceForHundredPercentOfRecipe
//
//class HalfProductDetailedListViewAdapter(
//    private val context: Activity,
//    private val list: List<Any>,
//    private val quantity: Double,
//    private val totalWeightOfMainRecipe: Double
//) : ArrayAdapter<Any>(
//    context,
//    R.layout.listview_dish_row,
//    list
//) {
//    private var _binding : ListviewDishRowBinding? = null
//    private val binding : ListviewDishRowBinding get() = _binding!!
//
//    @SuppressLint("ViewHolder")
//    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
//        val inflater = context.layoutInflater
//        _binding = ListviewDishRowBinding.inflate(inflater,parent,false)
//
//        val quantityPercent =
//            getBasicRecipeAsPercentageOfTargetRecipe(quantity, totalWeightOfMainRecipe)
//        val weightIncludedQuantity = getIngredientForHundredPercentOfRecipe(
//            list[position].weight,
//            quantityPercent
//        )
//        val formattedWeight = formatPriceOrWeight(weightIncludedQuantity)
//        val priceIncludedQuantity = getPriceForHundredPercentOfRecipe(
//            list[position].totalPriceOfThisProduct,
//            quantityPercent
//        )
//        val formattedPrice = Utils.formatPrice(priceIncludedQuantity,context)
//        setRowTextViews(position,formattedWeight,formattedPrice)
//        setRowAsClickListener(binding.root,position)
//        return binding.root
//    }
//
//    private fun setRowAsClickListener(rowView : View, position: Int){
//        rowView.setOnClickListener {
//            if (list[position].weightUnit == "piece") {
//                Toast.makeText(context, totalWeightMessage(position), Toast.LENGTH_SHORT).show()
//            } else Toast.makeText(
//                context,
//                list[position].productIncluded.name,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    private fun setRowTextViews(position: Int, formattedWeight : String, formatedPrice : String){
//        binding.productNameInDishRow.text =
//            list[position].productIncluded.name
//        binding.productWeightInDishRow.text = formattedWeight
//        binding.productPriceInDishRow.text = formatedPrice
//        binding.productWeightUnitInDishRow.text =
//            getUnitAbbreviation(list[position].weightUnit)
//    }
//
//    private fun totalWeightMessage(position: Int):String {
//        val isWeight =
//            list[position].halfProduct.halfProductUnit == "per kilogram" ||
//                    list[position].halfProduct.halfProductUnit == "per pound"
//        val unitType: String = if (isWeight) " of weight " else " of volume "
//        return list[position].productIncluded.name +
//                unitType +
//                list[position].totalWeightForPiece.toString() + " " +
//                getUnitAbbreviation(
//                    list[position].halfProduct.halfProductUnit.drop(
//                        4
//                    )
//                ) + "."
//    }
//}
