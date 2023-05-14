package com.erdees.foodcostcalc.utils.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.erdees.foodcostcalc.domain.model.product.ProductModel

class ProductDiffUtil(
  private val oldList : List<ProductModel>,
  private val newList : List<ProductModel>
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
     return when {
       oldList[oldItemPosition].productId != newList[newItemPosition].productId -> {
         false
       }

       oldList[oldItemPosition].pricePerUnit != newList[newItemPosition].pricePerUnit -> {
         false
       }
       oldList[oldItemPosition].name != newList[newItemPosition].name -> {
         false
       }

       oldList[oldItemPosition].tax != newList[newItemPosition].tax -> {
         false
       }

       oldList[oldItemPosition].waste != newList[newItemPosition].waste -> {
         false
       }

       oldList[oldItemPosition].unit != newList[newItemPosition].unit -> {
         false
       }

       else -> true
     }
   }

 }
