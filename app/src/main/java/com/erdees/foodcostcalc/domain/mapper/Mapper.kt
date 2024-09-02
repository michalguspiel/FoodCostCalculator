package com.erdees.foodcostcalc.domain.mapper

import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.data.model.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.associations.ProductDish
import com.erdees.foodcostcalc.data.model.associations.ProductHalfProduct
import com.erdees.foodcostcalc.data.model.joined.CompleteDish
import com.erdees.foodcostcalc.data.model.joined.HalfProductUsedInDish
import com.erdees.foodcostcalc.data.model.joined.CompleteHalfProduct
import com.erdees.foodcostcalc.data.model.joined.ProductAndProductDish
import com.erdees.foodcostcalc.data.model.joined.ProductUsedInHalfProduct
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.EditableProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain

object Mapper {
    fun CompleteDish.toDishDomain(): DishDomain {
        return DishDomain(
            dishId = dish.dishId,
            name = dish.name,
            marginPercent = dish.marginPercent,
            taxPercent = dish.dishTax,
            products = products.map { it.toUsedProductDomain() },
            halfProducts = halfProducts.map { it.toUsedHalfProductDomain() }
        )
    }

    fun ProductBase.toProductDomain(): ProductDomain {
        return ProductDomain(
            id = productId,
            name = name,
            pricePerUnit = pricePerUnit,
            tax = tax,
            waste = waste,
            unit = unit
        )
    }

    fun ProductDomain.toProductBase(): ProductBase {
        return ProductBase(
            productId = id,
            name = name,
            pricePerUnit = pricePerUnit,
            tax = tax,
            waste = waste,
            unit = unit
        )
    }

    fun ProductDomain.toEditableProductDomain(): EditableProductDomain {
        return EditableProductDomain(
            id = id,
            name = name,
            pricePerUnit = pricePerUnit.toString(),
            tax = tax.toString(),
            waste = waste.toString(),
            unit = unit
        )
    }

    /**
     * Maps EditableProductDomain to ProductBase
     * @return ProductBase
     * @throws NumberFormatException if any of the values in EditableProductDomain cannot be converted to Double
     * */
    fun EditableProductDomain.toProductBase(): ProductBase {
        return ProductBase(
            productId = id,
            name = name,
            pricePerUnit = pricePerUnit.toDouble(),
            tax = tax.toDouble(),
            waste = waste.toDouble(),
            unit = unit
        )
    }

    fun HalfProductBase.toHalfProductDomain(): HalfProductDomain {
        return HalfProductDomain(
            id = halfProductId,
            name = name,
            halfProductUnit = halfProductUnit,
            products = emptyList()
        )
    }

    fun HalfProductDomain.toHalfProductBase(): HalfProductBase {
        return HalfProductBase(
            halfProductId = id,
            name = name,
            halfProductUnit = halfProductUnit
        )
    }

    fun ProductAndProductDish.toUsedProductDomain(): UsedProductDomain {
        return UsedProductDomain(
            id = productDish.productDishId,
            ownerId = productDish.dishId,
            item = product.toProductDomain(),
            quantity = productDish.quantity,
            quantityUnit = productDish.quantityUnit,
            weightPiece = null
        )
    }

    fun ProductUsedInHalfProduct.toUsedProductDomain(): UsedProductDomain {
        return UsedProductDomain(
            id = productHalfProduct.productHalfProductId,
            ownerId = productHalfProduct.halfProductId,
            item = product.toProductDomain(),
            quantity = productHalfProduct.quantity,
            quantityUnit = productHalfProduct.quantityUnit,
            weightPiece = productHalfProduct.weightPiece
        )
    }


    fun CompleteHalfProduct.toHalfProductDomain(): HalfProductDomain {
        return HalfProductDomain(
            id = halfProductBase.halfProductId,
            name = halfProductBase.name,
            halfProductUnit = halfProductBase.halfProductUnit,
            products = products.map { it.toUsedProductDomain() }
        )
    }

    fun HalfProductUsedInDish.toUsedHalfProductDomain(): UsedHalfProductDomain {
        return UsedHalfProductDomain(
            id = halfProductDish.halfProductDishId,
            ownerId = halfProductDish.dishId,
            item = halfProductsWithProductsBase.toHalfProductDomain(),
            quantity = halfProductDish.quantity,
            quantityUnit = halfProductDish.quantityUnit
        )
    }

    fun UsedHalfProductDomain.toHalfProductDish(): HalfProductDish {
        return HalfProductDish(
            halfProductDishId = id,
            halfProductId = item.id,
            dishId = ownerId,
            quantity = quantity,
            quantityUnit = quantityUnit
        )
    }

    fun UsedProductDomain.toProductDish(): ProductDish {
        return ProductDish(
            productDishId = id,
            productId = item.id,
            dishId = ownerId,
            quantity = quantity,
            quantityUnit = quantityUnit
        )
    }

    fun UsedProductDomain.toProductHalfProduct(): ProductHalfProduct {
        return ProductHalfProduct(
            productHalfProductId = id,
            productId = item.id,
            halfProductId = ownerId,
            quantity = quantity,
            quantityUnit = quantityUnit,
            weightPiece = weightPiece
        )
    }

    fun DishDomain.toDishBase(): DishBase {
        return DishBase(
            dishId = dishId,
            name = name,
            marginPercent = marginPercent,
            dishTax = taxPercent
        )
    }
}
