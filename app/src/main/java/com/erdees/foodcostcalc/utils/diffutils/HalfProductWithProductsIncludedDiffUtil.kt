package com.erdees.foodcostcalc.utils.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductWithProductsIncluded

class HalfProductWithProductsIncludedDiffUtil(
  private val oldList : List<HalfProductWithProductsIncluded>,
  private val newList : List<HalfProductWithProductsIncluded>
): DiffUtil.Callback(){
  override fun getOldListSize(): Int {
    return oldList.size
  }

  override fun getNewListSize(): Int {
    return newList.size
  }

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].halfProduct.halfProductId == newList[newItemPosition].halfProduct.halfProductId
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return when {
      oldList[oldItemPosition].halfProduct != newList[newItemPosition].halfProduct -> {
        false
      }
      oldList[oldItemPosition].halfProductsList != newList[newItemPosition].halfProductsList -> {
        false
      }

      else -> true
    }
  }
}
