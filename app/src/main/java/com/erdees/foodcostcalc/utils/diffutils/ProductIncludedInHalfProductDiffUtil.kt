package com.erdees.foodcostcalc.utils.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProduct

 class ProductIncludedInHalfProductDiffUtil(
  private val oldList : List<ProductIncludedInHalfProduct>,
  private val newList : List<ProductIncludedInHalfProduct>
): DiffUtil.Callback(){
   override fun getOldListSize(): Int {
     return oldList.size
   }

   override fun getNewListSize(): Int {
     return newList.size
   }

   override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
    return oldList[oldItemPosition].productIncludedInHalfProductId == newList[newItemPosition].productIncludedInHalfProductId
   }

   override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
     return when {
       oldList[oldItemPosition].productIncludedInHalfProductId != newList[newItemPosition].productIncludedInHalfProductId -> {
         false
       }

       oldList[oldItemPosition].productModelIncluded != newList[newItemPosition].productModelIncluded -> {
         false
       }

       oldList[oldItemPosition].halfProductModel != newList[newItemPosition].halfProductModel -> {
         false
       }

       oldList[oldItemPosition].halfProductHostId != newList[newItemPosition].halfProductHostId -> {
         false
       }

       oldList[oldItemPosition].weight != newList[newItemPosition].weight -> {
         false
       }

       oldList[oldItemPosition].weightUnit != newList[newItemPosition].weightUnit -> {
         false
       }

       oldList[oldItemPosition].weightOfPiece != newList[newItemPosition].weightOfPiece -> {
         false
       }

       else -> true
     }
   }

 }
