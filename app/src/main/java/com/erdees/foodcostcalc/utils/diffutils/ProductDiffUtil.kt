package com.erdees.foodcostcalc.utils.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.erdees.foodcostcalc.entities.Product

class ProductDiffUtil(
  private val oldList : List<Product>,
  private val newList : List<Product>
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
