package com.erdees.foodcostcalc.domain.usecase

import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
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
     * Creates a new ProductBase entity from the form data, saves it to the product repository,
     * and returns the corresponding ProductDomain object.
     *
     * @param productName The name of the product to create
     * @param formData The data captured from the new product form
     * @return Result containing the ProductDomain representation of the newly created product
     */
    suspend operator fun invoke(
        productName: String,
        formData: NewProductFormData
    ): Result<ProductDomain> = withContext(myDispatchers.ioDispatcher) {
        try {
            val price = formData.purchasePrice.toDoubleOrNull()
                ?: throw InvalidProductPriceException("Product purchase price cannot be empty or invalid.")
            formData.purchaseUnit
                ?: throw IllegalArgumentException("Purchase unit must be provided.")

            val productBase = ProductBase(
                productId = 0,
                name = productName,
                tax = 0.0,
                waste = formData.wastePercent.toDoubleOrNull() ?: 0.0,
                canonicalUnit = formData.purchaseUnit,
                inputMethod = InputMethod.UNIT,
                packagePrice = null,//todo
                packageQuantity = null,//todo
                packageUnit = null,
                canonicalPrice = price,//todo
            )

            val newProductId = productRepository.addProduct(productBase)
            val createdProduct = productBase.copy(productId = newProductId).toProductDomain()

            Result.success(createdProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend operator fun invoke(
        unitPriceState: UnitPriceState
    ): Result<ProductDomain> = withContext(myDispatchers.ioDispatcher) {
        try {
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

            val newProductId = productRepository.addProduct(productBase)
            val createdProduct = productBase.copy(productId = newProductId).toProductDomain()

            Result.success(createdProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend operator fun invoke(
        packagePriceState: PackagePriceState,
    ): Result<ProductDomain> = withContext(myDispatchers.ioDispatcher) {
        try {
            with(packagePriceState) {
                val price = packagePriceState.packagePrice.toDoubleOrNull()
                    ?: throw InvalidProductPriceException("Product purchase price cannot be empty or invalid.")
                val packageQuantity = packagePriceState.packageQuantity.toDoubleOrNull()
                    ?: throw InvalidProductPriceException("Product package quantity cannot be empty or invalid.")

                // Prevent division by zero
                if (packageQuantity <= 0) {
                    throw InvalidProductPriceException("Package quantity must be greater than zero.")
                }

                val (canonicalPrice, canonicalUnit) = packageUnit.calculateCanonicalPrice(
                    packagePrice = price,
                    packageQuantity = packageQuantity
                )

                val productBase = ProductBase(
                    productId = 0,
                    name = name,
                    tax = tax.toDoubleOrNull() ?: 0.0,
                    waste = waste.toDoubleOrNull() ?: 0.0,
                    canonicalUnit = canonicalUnit,
                    inputMethod = InputMethod.PACKAGE,
                    packagePrice = price,
                    packageQuantity = packageQuantity,
                    packageUnit = packageUnit,
                    canonicalPrice = canonicalPrice,
                )
                val newProductId = productRepository.addProduct(productBase)
                val createdProduct = productBase.copy(productId = newProductId).toProductDomain()
                Result.success(createdProduct)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
