package com.erdees.foodcostcalc.utils.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductWithProductsIncludedModel

class HalfProductWithProductsIncludedDiffUtil(
  private val oldList : List<HalfProductWithProductsIncludedModel>,
  private val newList : List<HalfProductWithProductsIncludedModel>
): DiffUtil.Callback(){
  override fun getOldListSize(): Int {
    return oldList.size
  }

  override fun getNewListSize(): Int {
    return newList.size
  }

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].halfProductModel.halfProductId == newList[newItemPosition].halfProductModel.halfProductId
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return when {
      oldList[oldItemPosition].halfProductModel != newList[newItemPosition].halfProductModel -> {
        false
      }
      oldList[oldItemPosition].halfProductsList != newList[newItemPosition].halfProductsList -> {
        false
      }

      else -> true
    }
  }
}
