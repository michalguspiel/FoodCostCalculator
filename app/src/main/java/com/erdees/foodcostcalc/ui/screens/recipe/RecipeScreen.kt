package com.erdees.foodcostcalc.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEditableRecipe
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.model.recipe.EditableRecipe
import com.erdees.foodcostcalc.domain.model.recipe.RecipeDomain
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCOutlinedButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.labels.SectionLabel
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.EditDishViewModel
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.RecipeUpdater
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.RecipeViewMode
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun RecipeScreen(navController: NavController, viewModel: EditDishViewModel) {

    val dish by viewModel.dish.collectAsState()
    val recipe by viewModel.recipe.collectAsState()
    val recipeViewMode by viewModel.recipeViewModeState.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val recipeUpdater = RecipeUpdater(
        updatePrepTime = viewModel::updatePrepTime,
        updateCookTime = viewModel::updateCookTime,
        updateDescription = viewModel::updateDescription,
        updateTips = viewModel::updateTips,
        updateStep = viewModel::updateStep
    )

    RecipeScreenContent(
        recipeViewMode = recipeViewMode,
        dish = dish,
        recipe = recipe,
        screenState = screenState,
        modifier = Modifier,
        popBackStack = navController::popBackStack,
        toggleRecipeViewModel = viewModel::toggleRecipeViewMode,
        recipeUpdater = recipeUpdater,
        saveRecipe = viewModel::saveRecipe,
        cancelEdit = viewModel::cancelEdit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeScreenContent(
    recipeViewMode: RecipeViewMode,
    dish: DishDomain?,
    recipe: EditableRecipe,
    screenState: ScreenState,
    modifier: Modifier = Modifier,
    popBackStack: () -> Unit = {},
    toggleRecipeViewModel: () -> Unit = {},
    recipeUpdater: RecipeUpdater,
    saveRecipe: () -> Unit,
    cancelEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recipe),
                        modifier = Modifier
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp)
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (recipeViewMode) {
                    RecipeViewMode.VIEW -> {
                        dish?.recipe?.let { recipe ->
                            RecipeView(dish.products, dish.halfProducts, recipe)
                        } ?: RecipeMissingView()
                    }

                    RecipeViewMode.EDIT -> RecipeEdit(recipe, Modifier, recipeUpdater,saveRecipe, cancelEdit)
                }
            }

            when (screenState) {
                is ScreenState.Success -> {}
                is ScreenState.Error -> {}
                is ScreenState.Loading -> ScreenLoadingOverlay()
                else -> {}
            }
        }
    }
}


@Composable
private fun RecipeView(
    products: List<UsedProductDomain>,
    halfProducts: List<UsedHalfProductDomain>,
    recipe: RecipeDomain,
    modifier: Modifier = Modifier,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column {
                Text("Prep time")
                Row {
                    DecorativeCircle()
                    Text("${recipe.prepTimeMinutes} min")
                }
            }
            Column {
                Text("Cook time")
                Row {
                    DecorativeCircle()
                    Text("${recipe.prepTimeMinutes} min")
                }
            }
        }
    }
}

@Composable
private fun DecorativeCircle(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
private fun RecipeMissingView(modifier: Modifier = Modifier) {
    Column(modifier) {
        Text("Something about recipe missing.")
        Text("Suggestion to provide recipe.")
    }
}

@Composable
private fun RecipeEdit(
    recipe: EditableRecipe,
    modifier: Modifier = Modifier,
    recipeUpdater: RecipeUpdater,
    saveRecipe: () -> Unit,
    cancelEdit: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        SectionLabel(stringResource(R.string.general_info_title))

        FCCTextField(
            modifier = Modifier,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            singleLine = false,
            maxLines = 10,
            title = stringResource(id = R.string.description),
            value = recipe.description,
            onValueChange = { recipeUpdater.updateDescription(it) })

        FCCTextField(
            modifier = Modifier,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            singleLine = false,
            maxLines = 3,
            title = stringResource(id = R.string.tips),
            value = recipe.tips,
            onValueChange = { recipeUpdater.updateTips(it) })

        SectionLabel(stringResource(R.string.time_title))

        FCCTextField(
            modifier = Modifier,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            title = stringResource(id = R.string.prep_time),
            value = recipe.prepTimeMinutes,
            onValueChange = { recipeUpdater.updatePrepTime(it) })

        FCCTextField(
            modifier = Modifier,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            title = stringResource(id = R.string.cook_time),
            value = recipe.cookTimeMinutes,
            onValueChange = { recipeUpdater.updateCookTime(it) })


        SectionLabel(stringResource(R.string.steps_title))

        repeat(recipe.steps.size + 1) { index ->
            FCCTextField(
                modifier = Modifier,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                singleLine = false,
                maxLines = 5,
                title = stringResource(id = R.string.step_number, (index + 1).toString()),
                value = recipe.steps.getOrElse(index) { "" },
                onValueChange = { recipeUpdater.updateStep(index, it) })
        }

        ButtonRow(primaryButton = {
            FCCPrimaryButton(text = stringResource(R.string.save),modifier = Modifier, enabled = true, onClick = {
                saveRecipe()
            })
        }, secondaryButton = {
            FCCOutlinedButton(text = stringResource(R.string.cancel), enabled = true, onClick = {
                cancelEdit()
            })
        })
    }
}

private val previewDish = DishDomain(
    0L,
    "Fluffy Pancakes",
    200.0,
    23.0,
    products = listOf(),
    halfProducts = listOf(),
    recipe =
    RecipeDomain(
        recipeId = 0L,
        prepTimeMinutes = 10,
        cookTimeMinutes = 50,
        description = "These fluffy and golden pancakes are a breakfast classic! Perfect for a lazy weekend morning or a quick weekday treat, this recipe uses simple pantry ingredients and is easy to make. Serve with maple syrup, fresh berries, or your favorite toppings for a delicious start to your day.",
        tips = "Don't Overmix: Mix the batter until just combined; small lumps are fine. Overmixing can make the pancakes tough.\n" +
                "Rest the Batter: Let the batter rest for 5-10 minutes before cooking to allow the gluten to relax and ensure fluffier pancakes.\n" +
                "Use Medium Heat: Cooking on medium heat helps the pancakes cook through without burning.\n" +
                "Grease the Pan Lightly: Use a small amount of butter or oil and wipe off the excess with a paper towel for an evenly browned pancake",
        steps = listOf(
            "In a large bowl, whisk together 1 cup of all-purpose flour, 1 tablespoon of sugar, 1 teaspoon of baking powder, and ½ teaspoon of salt. " +
                    "In another bowl, combine 1 cup of milk, 1 egg, and 2 tablespoons of melted butter. " +
                    "Pour the wet ingredients into the dry ingredients and mix until just combined. Avoid overmixing.",
            "Heat a non-stick skillet or griddle over medium heat. Lightly grease the surface with butter or oil.",
            "Pour ¼ cup of batter onto the pan for each pancake. Cook until bubbles form on the surface and the edges look set (about 2 minutes). " +
                    "Flip the pancake and cook the other side until golden brown (about 1-2 minutes).",
            "Transfer cooked pancakes to a plate and cover with a clean towel or keep warm in a 90°C (200°F) oven while cooking the rest of the batter." +
                    "Stack the pancakes on a plate and serve with your favorite toppings, such as maple syrup, fresh fruit, whipped cream, or chocolate chips."
        )
    )
)


@Preview
@Composable
private fun RecipeScreenContentViewPreview() {
    FCCTheme {
        RecipeScreenContent(
            recipeViewMode = RecipeViewMode.VIEW,
            dish = previewDish,
            recipe = previewDish.recipe.toEditableRecipe(),
            screenState = ScreenState.Idle,
            modifier = Modifier,
            toggleRecipeViewModel = {},
            recipeUpdater = RecipeUpdater({}, {}, {}, {}, {_, _ -> }),
            saveRecipe = {},
            cancelEdit = {}
        )
    }
}

@Preview
@Composable
private fun RecipeScreenContentEditPreview() {
    FCCTheme {
        RecipeScreenContent(
            recipeViewMode = RecipeViewMode.EDIT,
            dish = previewDish,
            recipe = previewDish.recipe.toEditableRecipe(),
            screenState = ScreenState.Idle,
            modifier = Modifier,
            toggleRecipeViewModel = {},
            recipeUpdater = RecipeUpdater({}, {}, {}, {}, {_, _ -> }),
            saveRecipe = {},
            cancelEdit = {}
        )
    }
}