package com.erdees.foodcostcalc.ext

import android.content.Context
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.dish.DishDomain

fun DishDomain.toShareableText(
    context: Context,
    portions: Int = 1,
    showIngredientPrices: Boolean = false,
    showFinancialDetails: Boolean = false,
): String {
    val builder = StringBuilder()
    builder.appendLine(name)
    builder.appendLine()
    products.forEach { product ->
        builder.append("${product.item.name} ${product.quantity * portions} ${product.quantityUnit}.")
        if (showIngredientPrices) {
            builder.append(" ${product.totalPrice}.")
        }
        builder.appendLine()
    }

    halfProducts.forEach { halfProduct ->
        builder.append("${halfProduct.item.name} ${halfProduct.quantity * portions} ${halfProduct.quantityUnit}.")
        if (showIngredientPrices) {
            builder.append(" ${halfProduct.totalPrice}.")
        }
        builder.appendLine()
    }

    builder.appendLine()

    recipe?.let { recipe ->
        builder.appendLine(context.getString(R.string.recipe))
        recipe.description?.let { desc -> builder.appendLine(desc) }
        builder.appendLine()
        recipe.prepTimeMinutes?.let { prep ->
            builder.appendLine(
                context.getString(
                    R.string.recipe_sharing_prep_time,
                    prep.toString()
                )
            )
        }
        recipe.cookTimeMinutes?.let { cook ->
            builder.appendLine(
                context.getString(
                    R.string.recipe_sharing_cook_time,
                    cook.toString()
                )
            )
        }
        builder.appendLine()

        if (recipe.steps?.isNotEmpty() == true) {
            builder.appendLine(context.getString(R.string.steps_title))
            recipe.steps.sortedBy { step -> step.order }.forEach { step ->
                builder.appendLine("${step.order + 1 }. ${step.stepDescription}")
            }
            builder.appendLine()
        }

        recipe.tips?.let { tips ->
            if(tips.isNotBlank()){
                builder.appendLine(
                    context.getString(
                        R.string.recipe_sharing_tips,
                        tips
                    )
                )
            }
        }
    }

    if (showFinancialDetails) {
        builder.appendLine(
            context.getString(
                R.string.recipe_sharing_margin,
                marginPercent.toString()
            )
        )

        builder.appendLine(
            context.getString(
                R.string.recipe_sharing_food_cost,
                formattedFoodCostPerServings(amountOfServings = portions, context = context)
            )
        )
        builder.appendLine(
            context.getString(
                R.string.recipe_sharing_food_cost,
                formattedFoodCostPerServings(amountOfServings = portions, context = context)
            )
        )
    }

        builder.appendLine()
        builder.appendLine(context.getString(R.string.recipe_sharing_generated_with_fcc))
        builder.append("https://play.google.com/store/apps/details?id=com.erdees.foodcostcalc&pcampaignid=web_share")

    return builder.toString()
}