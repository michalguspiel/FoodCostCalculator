package com.erdees.foodcostcalc.ui.spotlight

enum class SpotlightStep(
    val info: String,
    val shape: SpotlightShape = SpotlightShape.RoundedRectangle,
    val hasNextButton: Boolean = false
) {
    ExampleDishCard(
        info = "Here's your example dish! Tap it to see more details.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = false
    ),
    DishDetails(
        info = "This section shows key financial information for your dish. You can set margin and tax percentages, and adjust the number of portions.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true
    ),
    IngredientsList(
        info = "Here you can see all the ingredients in your dish and their costs.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true
    ),
    PriceSummary(
        info = "This shows the food cost and final price for your dish.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true
    ),
    AddIngredientsButton(
        info = "Add ingredients to your dish by clicking here!",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true
    ),
    DetailsButton(
        info = "View and edit all dish details by clicking here.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true
    ),
    CreateDishFAB(
        info = "Now, let's create your first dish by clicking here!",
        shape = SpotlightShape.Circle,
        hasNextButton = false
    ),

    // CreateDishStartScreen steps
    DishNameField(
        info = "First, give your new dish a name.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true
    ),
    IngredientNameField(
        info = "Now, let's add the first ingredient. Start typing its name here.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true
    ),
    AddIngredientButton(
        info = "Once you've entered the ingredient name, click here to add it.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = false
    ),
    AddedIngredientsList(
        info = "Your added ingredients will appear here.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = true
    ),
    ContinueToSummaryButton(
        info = "When you've added all your ingredients, click here to continue.",
        shape = SpotlightShape.RoundedRectangle,
        hasNextButton = false
    );

    fun toSpotlightTarget(onClickAction: (() -> Unit)? = null) = SpotlightTarget(
        order = ordinal,
        info = info,
        rect = null,
        shape = shape,
        hasNextButton = hasNextButton,
        onClickAction = onClickAction
    )
}