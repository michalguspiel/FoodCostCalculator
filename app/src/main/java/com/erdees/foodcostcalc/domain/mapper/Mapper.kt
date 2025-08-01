package com.erdees.foodcostcalc.domain.mapper

import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.FeatureRequestEntity
import com.erdees.foodcostcalc.data.model.local.HalfProductBase
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.Recipe
import com.erdees.foodcostcalc.data.model.local.RecipeStep
import com.erdees.foodcostcalc.data.model.local.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.model.local.associations.ProductHalfProduct
import com.erdees.foodcostcalc.data.model.local.joined.CompleteDish
import com.erdees.foodcostcalc.data.model.local.joined.CompleteHalfProduct
import com.erdees.foodcostcalc.data.model.local.joined.HalfProductUsedInDish
import com.erdees.foodcostcalc.data.model.local.joined.ProductAndProductDish
import com.erdees.foodcostcalc.data.model.local.joined.ProductUsedInHalfProduct
import com.erdees.foodcostcalc.data.model.local.joined.RecipeWithSteps
import com.erdees.foodcostcalc.data.model.remote.FeatureRequest
import com.erdees.foodcostcalc.data.model.remote.FeatureRequestStatus
import com.erdees.foodcostcalc.domain.model.FeatureRequestDomain
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductAddedToDish
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.EditableProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.model.recipe.EditableRecipe
import com.erdees.foodcostcalc.domain.model.recipe.RecipeDomain
import com.erdees.foodcostcalc.domain.model.recipe.RecipeStepDomain
import com.erdees.foodcostcalc.utils.Formatter
import java.util.Date

object Mapper {
    fun CompleteDish.toDishDomain(): DishDomain {
        return DishDomain(
            id = dish.dishId,
            name = dish.name,
            marginPercent = dish.marginPercent,
            taxPercent = dish.dishTax,
            products = products.map { it.toUsedProductDomain() },
            halfProducts = halfProducts.map { it.toUsedHalfProductDomain() },
            recipe = recipe?.toRecipeDomain()
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

    private fun ProductAndProductDish.toUsedProductDomain(): UsedProductDomain {
        return UsedProductDomain(
            id = productDish.productDishId,
            ownerId = productDish.dishId,
            item = product.toProductDomain(),
            quantity = productDish.quantity,
            quantityUnit = productDish.quantityUnit,
            weightPiece = null
        )
    }

    private fun ProductUsedInHalfProduct.toUsedProductDomain(): UsedProductDomain {
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
            dishId = id,
            name = name,
            marginPercent = marginPercent,
            dishTax = taxPercent,
            recipeId = recipe?.recipeId
        )
    }

    fun RecipeWithSteps.toRecipeDomain(): RecipeDomain {
        return RecipeDomain(
            recipeId = recipe.recipeId,
            prepTimeMinutes = recipe.prepTimeMinutes,
            cookTimeMinutes = recipe.cookTimeMinutes,
            description = recipe.description,
            steps = steps.sortedBy { it.order }.map { it.toRecipeStepDomain() },
            tips = recipe.tips
        )
    }

    private fun RecipeStep.toRecipeStepDomain(): RecipeStepDomain {
        return RecipeStepDomain(id, order, stepDescription)
    }

    fun RecipeDomain?.toEditableRecipe(): EditableRecipe {
        return EditableRecipe(
            prepTimeMinutes = this?.prepTimeMinutes?.toString() ?: "",
            cookTimeMinutes = this?.cookTimeMinutes?.toString() ?: "",
            description = this?.description ?: "",
            tips = this?.tips ?: "",
            steps = this?.steps ?: listOf()
        )
    }

    fun RecipeWithSteps.toEditableRecipe(): EditableRecipe {
        return this.toRecipeDomain().toEditableRecipe()
    }

    fun RecipeDomain.toRecipe(id: Long? = null): Recipe {
        return Recipe(
            recipeId = id ?: 0,
            cookTimeMinutes = cookTimeMinutes,
            prepTimeMinutes = prepTimeMinutes,
            description = description,
            tips = tips
        )
    }

    fun RecipeDomain.toSteps() : List<RecipeStep> {
        return steps?.map { it.toRecipeStep(recipeId = recipeId ?: 0) } ?: emptyList()
    }


    fun EditableRecipe.toRecipe(id: Long?): Recipe {
        return Recipe(
            recipeId = id ?: 0,
            cookTimeMinutes = cookTimeMinutes.toIntOrNull(),
            prepTimeMinutes = prepTimeMinutes.toIntOrNull(),
            description = description,
            tips = tips
        )
    }

    fun RecipeStepDomain.toRecipeStep(recipeId: Long, newOrder: Int? = null): RecipeStep {
        return RecipeStep(
            id = id ?: 0,
            recipeId = recipeId,
            stepDescription = stepDescription,
            order = newOrder ?: order
        )
    }

    /**
     * Creates entity out of remote feature request data model. Takes generated id from firestore and
     * uses it as entity id. Takes timestamp from local device, as timestamp isn't so critical for this.
     * */
    fun FeatureRequest.toEntity(id: String, timeStamp: Date): FeatureRequestEntity {
        return FeatureRequestEntity(
            id = id,
            title = this.title,
            description = this.description,
            timestamp = timeStamp
        )
    }

    fun FeatureRequest.toFeatureRequestDomain(): FeatureRequestDomain? {
        val safeId = id
        val safeTimeStamp = timestamp

        return if (safeId == null || safeTimeStamp == null) {
            null
        } else {
            FeatureRequestDomain(
                id = safeId,
                title = title,
                description = description,
                status = getStatusEnum(),
                upVotes = upVotes,
                formattedTimeStamp = Formatter.formatTimeStamp(safeTimeStamp)
            )
        }
    }

    /**
     * Maps FeatureRequestEntity to FeatureRequest.
     * Approved defaults to false, status to PENDING, upVotes to 0, since essentially
     * these are values of remote object that isn't approved and displayed from remote.
     * */
    fun FeatureRequestEntity.toFeatureRequestDomain(): FeatureRequestDomain {
        return FeatureRequestDomain(
            id = id,
            title = title,
            description = description,
            formattedTimeStamp = Formatter.formatTimeStamp(timestamp),
            status = FeatureRequestStatus.PENDING,
            upVotes = 0
        )
    }

    fun ProductAddedToDish.toProductDish(dishId: Long): ProductDish {
        return ProductDish(
            productId = item.id,
            dishId = dishId,
            quantity = quantity,
            quantityUnit = quantityUnit,
            productDishId = AUTO_GENERATED_ID,
        )
    }

    fun HalfProductAddedToDish.toHalfProductDish(dishId: Long): HalfProductDish {
        return HalfProductDish(
            halfProductId = item.id,
            dishId = dishId,
            quantity = quantity,
            quantityUnit = quantityUnit,
            halfProductDishId = AUTO_GENERATED_ID,
        )
    }

    private const val AUTO_GENERATED_ID = 0L
}
