package com.erdees.foodcostcalc.domain.model.product

import com.erdees.foodcostcalc.domain.model.Item

data class EditableProductDomain(
    override val id: Long,
    override val name: String,
    val pricePerUnit: String,
    val tax: String,
    val waste: String,
    val unit: String
) : Item