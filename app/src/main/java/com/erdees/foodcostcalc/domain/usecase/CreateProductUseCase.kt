package com.erdees.foodcostcalc.domain.usecase

import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.errors.InvalidProductPriceException
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormData
import com.erdees.foodcostcalc.ui.screens.products.PackagePriceState
import com.erdees.foodcostcalc.ui.screens.products.UnitPriceState
import com.erdees.foodcostcalc.utils.MyDispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for creating a new product from form data and saving it to the repository.
 * This consolidates the product creation logic that was duplicated across multiple ViewModels.
 */
class CreateProductUseCase(
    private val productRepository: ProductRepository,
    private val myDispatchers: MyDispatchers
) {

    /**
     * Creates a new product from NewProductFormData.
     */
    suspend operator fun invoke(
        productName: String,
        formData: NewProductFormData
    ): Result<ProductDomain> = runCatchingOnIo {
        val (canonicalPrice, canonicalUnit) = calculateCanonicalPriceAndUnit(formData)
        val productBase = ProductBase(
            productId = 0,
            name = productName,
            tax = 0.0,
            waste = formData.wastePercent.toDoubleOrNull() ?: 0.0,
            canonicalPrice = canonicalPrice,
            canonicalUnit = canonicalUnit,
            inputMethod = formData.inputMethod,
            packagePrice = formData.packagePrice.toDoubleOrNull(),
            packageQuantity = formData.packageQuantity.toDoubleOrNull(),
            packageUnit = formData.packageUnit,
        )
        saveProduct(productBase)
    }

    /**
     * Creates a new product from UnitPriceState.
     */
    suspend operator fun invoke(
        unitPriceState: UnitPriceState
    ): Result<ProductDomain> = runCatchingOnIo {
        val price = unitPriceState.unitPrice.toDoubleOrNull()
            ?: throw InvalidProductPriceException("Product purchase price cannot be empty or invalid.")

        val productBase = ProductBase(
            productId = 0,
            name = unitPriceState.name,
            tax = unitPriceState.tax.toDoubleOrNull() ?: 0.0,
            waste = unitPriceState.waste.toDoubleOrNull() ?: 0.0,
            canonicalUnit = unitPriceState.unitPriceUnit,
            inputMethod = InputMethod.UNIT,
            packagePrice = null,
            packageQuantity = null,
            packageUnit = null,
            canonicalPrice = price,
        )
        saveProduct(productBase)
    }

    /**
     * Creates a new product from PackagePriceState.
     */
    suspend operator fun invoke(
        packagePriceState: PackagePriceState,
    ): Result<ProductDomain> = runCatchingOnIo {
        val price = packagePriceState.packagePrice.toDoubleOrNull()
            ?: throw InvalidProductPriceException("Product package price cannot be empty or invalid.")
        val packageQuantity = packagePriceState.packageQuantity.toDoubleOrNull()
            ?: throw InvalidProductPriceException("Product package quantity cannot be empty or invalid.")

        if (packageQuantity <= 0) {
            throw InvalidProductPriceException("Package quantity must be greater than zero.")
        }

        val (canonicalPrice, canonicalUnit) = packagePriceState.packageUnit.calculateCanonicalPrice(
            packagePrice = price,
            packageQuantity = packageQuantity
        )

        val productBase = ProductBase(
            productId = 0,
            name = packagePriceState.name,
            tax = packagePriceState.tax.toDoubleOrNull() ?: 0.0,
            waste = packagePriceState.waste.toDoubleOrNull() ?: 0.0,
            canonicalUnit = canonicalUnit,
            inputMethod = InputMethod.PACKAGE,
            packagePrice = price,
            packageQuantity = packageQuantity,
            packageUnit = packagePriceState.packageUnit,
            canonicalPrice = canonicalPrice,
        )
        saveProduct(productBase)
    }

    private fun calculateCanonicalPriceAndUnit(formData: NewProductFormData): Pair<Double, MeasurementUnit> {
        return when (formData.inputMethod) {
            InputMethod.PACKAGE -> {
                val (packageUnit, packagePrice, packageQuantity) = safeUnwrapPackageData(formData)
                packageUnit.calculateCanonicalPrice(
                    packagePrice = packagePrice,
                    packageQuantity = packageQuantity
                )
            }
            InputMethod.UNIT -> {
                val (unitPriceUnit, price) = safeUnwrapUnitData(formData)
                price to unitPriceUnit
            }
        }
    }

    private fun safeUnwrapPackageData(formData: NewProductFormData): Triple<MeasurementUnit, Double, Double> {
        val packageUnit = formData.packageUnit
        val packagePrice = formData.packagePrice.toDoubleOrNull()
        val packageQuantity = formData.packageQuantity.toDoubleOrNull()

        if (packageUnit == null || packagePrice == null || packageQuantity == null) {
            throw InvalidProductPriceException("Product package unit, price, and quantity must all be valid.")
        }

        return Triple(packageUnit, packagePrice, packageQuantity)
    }

    private fun safeUnwrapUnitData(formData: NewProductFormData): Pair<MeasurementUnit, Double> {
        val unitPriceUnit = formData.unitPriceUnit
        val price = formData.unitPrice.toDoubleOrNull()

        if (unitPriceUnit == null || price == null) {
            throw InvalidProductPriceException("Product unit price unit and price must both be valid.")
        }

        return unitPriceUnit to price
    }

    /**
     * Saves the product to the repository and returns the domain model.
     */
    private suspend fun saveProduct(productBase: ProductBase): ProductDomain {
        val newProductId = productRepository.addProduct(productBase)
        return productBase.copy(productId = newProductId).toProductDomain()
    }

    /**
     * Helper to run a block of code within a withContext and runCatching.
     */
    private suspend fun <T> runCatchingOnIo(block: suspend () -> T): Result<T> {
        return withContext(myDispatchers.ioDispatcher) {
            runCatching { block() }
        }
    }
}