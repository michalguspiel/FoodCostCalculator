package com.erdees.foodcostcalc.utils.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.erdees.foodcostcalc.domain.model.dish.GrandDish

class GrandDishDiffUtil(
  private val oldList : List<GrandDish>,
  private val newList : List<GrandDish>
): DiffUtil.Callback(){
  override fun getOldListSize(): Int {
    return oldList.size
  }

  override fun getNewListSize(): Int {
    return newList.size
  }

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].dish.dishId == newList[newItemPosition].dish.dishId
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return when {
      oldList[oldItemPosition].dish.dishTax != newList[newItemPosition].dish.dishTax -> {
        false
      }
      oldList[oldItemPosition].dish.name != newList[newItemPosition].dish.name -> {
        false
      }
      oldList[oldItemPosition].dish.marginPercent != newList[newItemPosition].dish.marginPercent -> {
        false
      }

      oldList[oldItemPosition].halfProducts != newList[newItemPosition].halfProducts -> {
        false
      }
      oldList[oldItemPosition].productsIncluded != newList[newItemPosition].productsIncluded -> {
        false
      }

      oldList[oldItemPosition].totalPrice != newList[newItemPosition].totalPrice -> {
        false
      }

      else -> true
    }
  }

}
