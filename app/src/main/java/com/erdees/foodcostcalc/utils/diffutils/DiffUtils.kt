package com.erdees.foodcostcalc.utils.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.erdees.foodcostcalc.domain.model.dish.DishDomain


class DishDomainDiffUtil(
  private val oldList: List<DishDomain>,
  private val newList: List<DishDomain>
) : DiffUtil.Callback() {
  override fun getOldListSize(): Int {
    return oldList.size
  }

  override fun getNewListSize(): Int {
    return newList.size
  }

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].dishId == newList[newItemPosition].dishId
  }

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition] == newList[newItemPosition]
  }
}

