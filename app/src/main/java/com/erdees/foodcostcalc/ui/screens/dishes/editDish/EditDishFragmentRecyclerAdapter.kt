package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.EditDishProductRowBinding
import com.erdees.foodcostcalc.utils.Utils.isNotBlankNorJustDot

class EditDishFragmentRecyclerAdapter(
  private val activity: Activity,
) : RecyclerView.Adapter<EditDishFragmentRecyclerAdapter.EditDishViewHolder>() {

  class EditDishViewHolder(val viewBinding: EditDishProductRowBinding) :
    RecyclerView.ViewHolder(viewBinding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditDishViewHolder {
    return EditDishViewHolder(
      EditDishProductRowBinding.inflate(
        LayoutInflater.from(activity),
        parent,
        false
      )
    )
  }

  override fun getItemCount(): Int {
    // TODO
    return Int.MAX_VALUE // for now,
//        return grandDish.halfProducts.size + grandDish.productsIncluded.size
  }

  fun save() {
//    todo
  }

  override fun onBindViewHolder(holder: EditDishViewHolder, position: Int) {
    // todo fix it
//    if (isRowAProduct(holder)) {
//      setFields(
//        grandDish.productsIncluded[holder.adapterPosition].productIncluded.name,
//        grandDish.productsIncluded[holder.adapterPosition].weight,
//        grandDish.productsIncluded[holder.adapterPosition].weightUnit,
//        holder
//      )
//      holder.viewBinding.deleteProductInDishButton.setOnClickListener {
//        viewModel.deleteProductIncluded(grandDish.productsIncluded[holder.adapterPosition])
//      }
//      addProductWeightTextChangedListener(holder)
//    } else if (isRowHalfProduct(holder)) {
//      val thisPosition =
//        holder.adapterPosition - grandDish.productsIncluded.size // to start counting position from new list
//      setFields(
//        grandDish.halfProducts[thisPosition].halfProduct.name,
//        grandDish.halfProducts[thisPosition].weight,
//        grandDish.halfProducts[thisPosition].unit,
//        holder
//      )
//      holder.viewBinding.deleteProductInDishButton.setOnClickListener {
//        viewModel.deleteHalfProductIncluded(grandDish.halfProducts[thisPosition])
//      }
//      addHalfProductWeightTextChangedListener(holder, thisPosition)
//  }
  }

  private fun addHalfProductWeightTextChangedListener(holder: EditDishViewHolder, pos: Int) {
    holder.viewBinding.productWeightEdittext.addTextChangedListener((object : TextWatcher {
      override fun afterTextChanged(s: Editable) {}
      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {
        if (s.isNotBlankNorJustDot()) {
          // todo whatever this was doing
//          if (pos < viewModel.cloneOfListOfHalfProductModels.size) {
//            viewModel.cloneOfListOfHalfProductModels[pos].weight =
//              s.toString().toDouble()
//          }
        }
      }

    }))
  }

  private fun setFields(name: String, weight: Double, unit: String, holder: EditDishViewHolder) {
    holder.viewBinding.productNameTextView.text = name
    holder.viewBinding.productWeightEdittext.setText(weight.toString())
    setUnit(unit, weight, holder)
  }

  private fun isRowHalfProduct(holder: EditDishViewHolder): Boolean = false
  // todo

  private fun isRowAProduct(holder: EditDishViewHolder): Boolean = false
  // todo

  private fun addProductWeightTextChangedListener(holder: EditDishViewHolder) {
    holder.viewBinding.productWeightEdittext.addTextChangedListener((object : TextWatcher {
      override fun afterTextChanged(s: Editable) {}

      override fun beforeTextChanged(
        s: CharSequence, start: Int,
        count: Int, after: Int
      ) {
      }

      override fun onTextChanged(
        s: CharSequence, start: Int,
        before: Int, count: Int
      ) {
        if (s.isNotBlankNorJustDot()) {
         // TODO
//          if (holder.adapterPosition < viewModel.cloneOfListOfProductsIncluded.size){
//            viewModel.cloneOfListOfProductsIncluded[holder.adapterPosition].weight =
//              s.toString().toDouble()
//          }
        }
      }
    }
      ))
  }

  private fun setUnit(
    result: String,
    weight: Double,
    holder: EditDishViewHolder
  ) {
    if (weight <= 1) holder.viewBinding.unitTextView.text = result
    else holder.viewBinding.unitTextView.text =
      activity.getString(R.string.string_plural, result)
  }
}
