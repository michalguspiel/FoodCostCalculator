package com.erdees.foodcostcalc.domain.model

interface UsedItem {
  val id: Long
  val ownerId: Long
  val item : Item
  val quantity : Double
  val quantityUnit: String
}
