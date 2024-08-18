package com.erdees.foodcostcalc.utils.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.domain.model.dish.DishDomain

class ProductDiffUtil(
    private val oldList : List<ProductBase>,
    private val newList : List<ProductBase>
): DiffUtil.Callback(){
   override fun getOldListSize(): Int {
     return oldList.size
   }

   override fun getNewListSize(): Int {
     return newList.size
   }

   override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].productId == newList[newItemPosition].productId
   }

   override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
     return oldList[oldItemPosition] == newList[newItemPosition]
   }
 }


class DishDomainDiffUtil(
  private val oldList : List<DishDomain>,
  private val newList : List<DishDomain>
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

//class UsedProductDomainDiffUtil(
//  private val oldList : List<UsedProductDomain>,
//  private val newList : List<UsedProductDomain>
//) : DiffUtil.Callback() {
//
//}
//
//class UsedHalfProductDomainDiffUtil(
//  private val oldList : List<UsedHalfProductDomain>,
//  private val newList : List<UsedHalfProductDomain>
//) : DiffUtil.Callback() {
//
//}
