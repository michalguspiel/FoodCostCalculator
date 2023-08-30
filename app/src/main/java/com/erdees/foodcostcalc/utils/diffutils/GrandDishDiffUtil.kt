package com.erdees.foodcostcalc.utils.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.erdees.foodcostcalc.domain.model.dish.GrandDishModel

class GrandDishDiffUtil(
  private val oldList : List<GrandDishModel>,
  private val newList : List<GrandDishModel>
): DiffUtil.Callback(){
  override fun getOldListSize(): Int {
    return oldList.size
  }

  override fun getNewListSize(): Int {
    return newList.size
  }

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].dishModel.dishId == newList[newItemPosition].dishModel.dishId
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return when {
      oldList[oldItemPosition].dishModel.dishTax != newList[newItemPosition].dishModel.dishTax -> {
        false
      }
      oldList[oldItemPosition].dishModel.name != newList[newItemPosition].dishModel.name -> {
        false
      }
      oldList[oldItemPosition].dishModel.marginPercent != newList[newItemPosition].dishModel.marginPercent -> {
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
