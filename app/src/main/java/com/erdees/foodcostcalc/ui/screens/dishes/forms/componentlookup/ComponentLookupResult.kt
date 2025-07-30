package com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup

import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain

data class ComponentLookupResult(
    val products: List<ProductDomain> = emptyList(),
    val halfProducts: List<HalfProductDomain> = emptyList()
) {
    val isEmpty: Boolean
        get() = products.isEmpty() && halfProducts.isEmpty()
}
