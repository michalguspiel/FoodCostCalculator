package com.erdees.foodcostcalc.ui.screens.recipe

import android.icu.util.Currency
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEditableRecipe
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.recipe.EditableRecipe
import com.erdees.foodcostcalc.domain.model.recipe.RecipeDomain
import com.erdees.foodcostcalc.domain.model.recipe.RecipeStepDomain
import com.erdees.foodcostcalc.ui.composables.Ingredients
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCOutlinedButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTextButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ValueEditDialog
import com.erdees.foodcostcalc.ui.composables.dividers.FCCSecondaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.labels.SectionLabel
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.DishDetailsViewModel
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.onIntegerValueChange
import timber.log.Timber
import java.util.Locale

data class RecipeScreenCallbacks(
    val popBackStack: () -> Unit = {},
    val toggleRecipeViewMode: () -> Unit = {},
    val saveRecipe: () -> Unit = {},
    val cancelEdit: () -> Unit = {},
    val onChangeServings: () -> Unit = {},
    val updateServings: (String) -> Unit = {},
    val resetScreenState: () -> Unit = {},
)

@Screen
@Composable
fun RecipeScreen(navController: NavController, viewModel: DishDetailsViewModel) {

    val dish by viewModel.dish.collectAsState()
    val recipe by viewModel.recipe.collectAsState()
    val recipeViewMode by viewModel.recipeViewModeState.collectAsState()
    val screenState by viewModel.screenState.collectAsState()
    val recipeUpdater = viewModel.recipeUpdater
    val recipeServings by viewModel.recipeServings.collectAsState()
    val recipeEvent by viewModel.recipeEvent.collectAsStateWithLifecycle(null)
    val currency by viewModel.currency.collectAsState()

    LaunchedEffect(recipeEvent) {
        Timber.i("New $recipeEvent received.")
        when (recipeEvent) {
            RecipeEvent.CancelEditRecipeMissing -> navController.popBackStack()
            else -> {}
        }
    }

    RecipeScreenContent(
        recipeViewMode = recipeViewMode,
        dish = dish,
        recipe = recipe,
        screenState = screenState,
        servings = recipeServings,
        currency = currency,
        recipeUpdater = recipeUpdater,
        modifier = Modifier,
        recipeScreenCallbacks = RecipeScreenCallbacks(
            popBackStack = navController::popBackStack,
            toggleRecipeViewMode = viewModel::toggleRecipeViewMode,
            saveRecipe = viewModel::saveRecipe,
            cancelEdit = viewModel::cancelRecipeEdit,
            onChangeServings = viewModel::onChangeServings,
            updateServings = viewModel::updateServings,
            resetScreenState = viewModel::resetScreenState
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeScreenContent(
    recipeViewMode: RecipeViewMode,
    dish: DishDomain?,
    recipe: EditableRecipe,
    screenState: ScreenState,
    servings: Int,
    currency: Currency?,
    recipeUpdater: RecipeUpdater,
    modifier: Modifier = Modifier,
    recipeScreenCallbacks: RecipeScreenCallbacks,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = {
                Text(
                    text = if (recipeViewMode == RecipeViewMode.VIEW) {
                        dish?.name ?: stringResource(R.string.recipe)
                    } else {
                        stringResource(R.string.edit_recipe)
                    }, modifier = Modifier
                )
            }, navigationIcon = {
                IconButton(onClick = { recipeScreenCallbacks.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Sharp.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            })
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {
            when (recipeViewMode) {
                RecipeViewMode.VIEW -> {
                    dish?.recipe?.let { recipe ->
                        RecipeView(
                            dish,
                            servings,
                            recipe,
                            currency,
                            toggleRecipeViewMode = { recipeScreenCallbacks.toggleRecipeViewMode() },
                            onChangeServings = { recipeScreenCallbacks.onChangeServings() })
                    } ?: RecipeMissingView()
                }

                RecipeViewMode.EDIT -> RecipeEdit(
                    recipe,
                    Modifier,
                    recipeUpdater,
                    recipeScreenCallbacks.saveRecipe,
                    recipeScreenCallbacks.cancelEdit
                )
            }
        }

        when (screenState) {
            is ScreenState.Success<*> -> {
                recipeScreenCallbacks.toggleRecipeViewMode()
            }

            is ScreenState.Error -> {}
            is ScreenState.Loading<*> -> ScreenLoadingOverlay()
            is ScreenState.Interaction -> {
                if (screenState.interaction == InteractionType.ChangeServings) {
                    val editable = remember { mutableStateOf(servings.toString()) }
                    ValueEditDialog(
                        title = stringResource(id = R.string.change_portions),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        value = editable.value,
                        updateValue = { newValue ->
                            editable.value =
                                onIntegerValueChange(editable.value, newValue)
                        },
                        onSave = {
                            recipeScreenCallbacks.updateServings(editable.value)
                        },
                        onDismiss = { recipeScreenCallbacks.resetScreenState() })
                }
            }

            else -> {}
        }
    }
}


@Composable
private fun RecipeView(
    dish: DishDomain,
    servings: Int,
    recipe: RecipeDomain,
    currency: Currency?,
    modifier: Modifier = Modifier,
    toggleRecipeViewMode: () -> Unit,
    onChangeServings: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Times(recipe, recipe.prepTimeMinutes, recipe.cookTimeMinutes)

            FCCSecondaryHorizontalDivider(Modifier.fillMaxWidth())

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionLabel(stringResource(R.string.description))
                recipe.description?.let {
                    Text(
                        recipe.description, Modifier.fillMaxWidth(), textAlign = TextAlign.Start
                    )
                }
            }

            val steps: String? = recipe.steps?.sortedBy { it.order }
                ?.joinToString(separator = "\n") { "${it.order + 1}. ${it.stepDescription}" }

            steps?.let {
                if (steps.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SectionLabel(stringResource(R.string.steps_title))
                        Text(steps, Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionLabel(stringResource(R.string.ingredients_title, servings.toString()))
                Ingredients(
                    dishDomain = dish,
                    servings = servings.toDouble(),
                    currency = currency,
                    modifier = Modifier,
                    showPrices = false
                )
                FCCTextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.change_portions)
                ) {
                    onChangeServings()
                }
            }

            Tips(recipe.tips)
        }

        ButtonRow(primaryButton = {
            FCCPrimaryButton(
                text = stringResource(R.string.edit_recipe),
                modifier = Modifier,
                enabled = true,
                onClick = { toggleRecipeViewMode() })
        })
    }
}

@Composable
private fun Tips(tips: String?, modifier: Modifier = Modifier) {
    tips?.let {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionLabel(stringResource(R.string.tips))
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(6.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = tips,
                    modifier = Modifier,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun Times(
    recipe: RecipeDomain, prepTimeMinutes: Int?, cookTimeMinutes: Int?
) {
    Row(
        Modifier.fillMaxWidth(0.8f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.prep_time_title))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DecorativeCircle()
                Spacer(Modifier.size(4.dp))
                Text(recipe.prepTimeMinutes?.let {
                    stringResource(
                        R.string.prep_time_value, prepTimeMinutes.toString()
                    )
                } ?: stringResource(R.string.not_specified))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.cook_time_title))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DecorativeCircle()
                Spacer(Modifier.size(4.dp))
                Text(recipe.cookTimeMinutes?.let {
                    stringResource(
                        R.string.cook_time_value, cookTimeMinutes.toString()
                    )
                } ?: stringResource(R.string.not_specified))
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
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.recipe_something_went_wrong))
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
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ),
                title = stringResource(id = R.string.prep_time),
                value = recipe.prepTimeMinutes,
                onValueChange = { recipeUpdater.updatePrepTime(it) })

            FCCTextField(
                modifier = Modifier,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
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
                    value = recipe.steps.getOrNull(index)?.stepDescription ?: "",
                    onValueChange = { recipeUpdater.updateStep(index, it) })
            }

            ButtonRow(
                modifier = Modifier,
                primaryButton = {
                    FCCPrimaryButton(
                        text = stringResource(R.string.save),
                        modifier = Modifier,
                        enabled = true,
                        onClick = {
                            saveRecipe()
                        })
                }, secondaryButton = {
                    FCCOutlinedButton(
                        text = stringResource(R.string.cancel),
                        enabled = true,
                        onClick = {
                            cancelEdit()
                        })
                })
        }
    }
}

private val previewSteps = listOf(
    RecipeStepDomain(
        null,
        0,
        "In a large bowl, whisk together 1 cup of all-purpose flour, 1 tablespoon of sugar, 1 teaspoon of baking powder, and ½ teaspoon of salt. " + "In another bowl, combine 1 cup of milk, 1 egg, and 2 tablespoons of melted butter. " + "Pour the wet ingredients into the dry ingredients and mix until just combined. Avoid overmixing."
    ),
    RecipeStepDomain(
        null,
        1,
        "Heat a non-stick skillet or griddle over medium heat. Lightly grease the surface with butter or oil.",
    ),
    RecipeStepDomain(
        null, 2,
        "Pour ¼ cup of batter onto the pan for each pancake. Cook until bubbles form on the surface and the edges look set (about 2 minutes). " + "Flip the pancake and cook the other side until golden brown (about 1-2 minutes).",
    ),
    RecipeStepDomain(
        null,
        3,
        "Transfer cooked pancakes to a plate and cover with a clean towel or keep warm in a 90°C (200°F) oven while cooking the rest of the batter." + "Stack the pancakes on a plate and serve with your favorite toppings, such as maple syrup, fresh fruit, whipped cream, or chocolate chips."
    ),
)


private val previewDish = DishDomain(
    0L,
    "Fluffy Pancakes",
    200.0,
    23.0,
    products = listOf(),
    halfProducts = listOf(),
    recipe = RecipeDomain(
        recipeId = 0L,
        prepTimeMinutes = 10,
        cookTimeMinutes = 50,
        description = "These fluffy and golden pancakes are a breakfast classic! Perfect for a lazy weekend morning or a quick weekday treat, this recipe uses simple pantry ingredients and is easy to make. Serve with maple syrup, fresh berries, or your favorite toppings for a delicious start to your day.",
        tips = "Don't Overmix: Mix the batter until just combined; small lumps are fine. Overmixing can make the pancakes tough.\n" + "Rest the Batter: Let the batter rest for 5-10 minutes before cooking to allow the gluten to relax and ensure fluffier pancakes.\n" + "Use Medium Heat: Cooking on medium heat helps the pancakes cook through without burning.\n" + "Grease the Pan Lightly: Use a small amount of butter or oil and wipe off the excess with a paper towel for an evenly browned pancake",
        steps = previewSteps
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
            servings = 1,
            currency = Currency.getInstance(Locale.getDefault()),
            modifier = Modifier,
            recipeUpdater = RecipeUpdater({}, {}, {}, {}, { _, _ -> }),
            recipeScreenCallbacks = RecipeScreenCallbacks(
                toggleRecipeViewMode = {},
                saveRecipe = {},
                cancelEdit = {},
                updateServings = {},
                popBackStack = {},
                onChangeServings = {},
                resetScreenState = {},
            )
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
            servings = 10,
            currency = Currency.getInstance(Locale.getDefault()),
            recipeUpdater = RecipeUpdater({}, {}, {}, {}, { _, _ -> }),
            modifier = Modifier,
            recipeScreenCallbacks = RecipeScreenCallbacks(
                toggleRecipeViewMode = {},
                saveRecipe = {},
                cancelEdit = {},
                updateServings = {},
                popBackStack = {},
                onChangeServings = {},
                resetScreenState = {},
            )
        )
    }
}