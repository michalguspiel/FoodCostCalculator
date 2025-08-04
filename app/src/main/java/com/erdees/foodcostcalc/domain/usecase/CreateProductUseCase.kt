package com.erdees.foodcostcalc.domain.usecase

import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.errors.InvalidProductPriceException
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormData
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
            formData.purchaseUnit ?: throw IllegalArgumentException("Purchase unit must be provided.")

            val productBase = ProductBase(
                productId = 0,
                name = productName,
                canonicalPricePerBaseUnit = price,
                tax = 0.0,
                waste = formData.wastePercent.toDoubleOrNull() ?: 0.0,
                unit = formData.purchaseUnit,
                inputMethod = InputMethod.UNIT,
                packagePrice = null,//todo
                packageQuantity = null,//todo
                packageUnit = null,//todo
            )

            val newProductId = productRepository.addProduct(productBase)
            val createdProduct = productBase.copy(productId = newProductId).toProductDomain()

            Result.success(createdProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
