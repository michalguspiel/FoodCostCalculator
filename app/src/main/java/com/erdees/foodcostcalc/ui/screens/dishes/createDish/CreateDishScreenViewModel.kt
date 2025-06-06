package com.erdees.foodcostcalc.ui.screens.dishes.createDish

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.domain.ProductDomain
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.UnitsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish


// Updated Data class for ingredients in the dish
data class DishIngredient(
    val productId: Long,
    val productName: String,
    val dishQuantity: MutableStateFlow<String> = MutableStateFlow("1"),
    val dishUnit: MutableStateFlow<String> = MutableStateFlow(UnitsUtils.GRAM), // Default to "g" or a common unit
    val originalProductPricePerUnit: Double, // Price of the product per its originalProductUnit (after waste and tax if applicable)
    val originalProductUnit: String, // The unit in which originalProductPricePerUnit is expressed
    val purchasePrice: Double? = null, // Raw purchase price, if newly created through dialog
    val purchaseUnit: String? = null,   // Raw purchase unit, if newly created through dialog
    val tempClientId: String = UUID.randomUUID().toString()
)

class CreateDishScreenViewModel : ViewModel(), KoinComponent {

    private val dispatchers: MyDispatchers by inject()
    private val dishRepository: DishRepository by inject()
    private val productRepository: ProductRepository by inject() // Injected ProductRepository
    private val analyticsRepository: AnalyticsRepository by inject()

    private val sharedPreferences: Preferences by inject()

    var dishName = MutableStateFlow("")
    val margin: MutableStateFlow<String> = MutableStateFlow(Constants.BASIC_MARGIN.toString())
    val tax: MutableStateFlow<String> = MutableStateFlow(Constants.BASIC_TAX.toString())

    // StateFlow for the "Add Ingredient" text field
    val newIngredientName = MutableStateFlow("")

    // StateFlow for the list of ingredients
    private val _ingredients = MutableStateFlow<List<DishIngredient>>(emptyList())
    val ingredients: StateFlow<List<DishIngredient>> = _ingredients.asStateFlow()

    // StateFlow for search results
    private val _suggestedIngredients = MutableStateFlow<List<ProductDomain>>(emptyList())
    val suggestedIngredients: StateFlow<List<ProductDomain>> = _suggestedIngredients.asStateFlow()

    // State for New Ingredient Dialog
    private val _showNewIngredientDialog = MutableStateFlow(false)
    val showNewIngredientDialog: StateFlow<Boolean> = _showNewIngredientDialog.asStateFlow()

    val newIngredientPurchasePrice = MutableStateFlow("")
    val newIngredientPurchaseUnit = MutableStateFlow(UnitsUtils.getUnitsData().first().abbreviation) // Default to first unit
    val newIngredientWastePercentage = MutableStateFlow("0")

    // To track if a suggestion was selected
    private val selectedSuggestedProduct = MutableStateFlow<ProductDomain?>(null)

    // StateFlows for Costs and Prices
    private val _foodCost = MutableStateFlow(0.0)
    val foodCost: StateFlow<Double> = _foodCost.asStateFlow()

    private val _finalPrice = MutableStateFlow(0.0)
    val finalPrice: StateFlow<Double> = _finalPrice.asStateFlow()


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    init {
        viewModelScope.launch(dispatchers.ioDispatcher) {
            // Initial cost calculation
            recalculateCosts()

            // Observe margin and tax for changes
            margin.collectLatest { if (it.isNotEmpty()) recalculateCosts() }
        }
        viewModelScope.launch(dispatchers.ioDispatcher) {
            tax.collectLatest { if (it.isNotEmpty()) recalculateCosts() }
        }

        viewModelScope.launch(dispatchers.ioDispatcher) {
            // Observe newIngredientName for search and to clear selectedSuggestedProduct
            newIngredientName
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (selectedSuggestedProduct.value?.name != query) { // If name changed manually
                        selectedSuggestedProduct.value = null
                    }
                    if (query.length >= 2) {
                        // Assuming productRepository.products is a Flow<List<ProductDomain>>
                        // This should ideally be productRepository.searchProductsByName(query)
                        productRepository.products.map { allProducts ->
                            allProducts.filter { product ->
                                product.name.contains(query, ignoreCase = true)
                            }
                        }.collectLatest { filteredProducts ->
                            _suggestedIngredients.value = filteredProducts
                        }
                    } else {
                        _suggestedIngredients.value = emptyList()
                    }
                }
        }
    }

    val addButtonEnabled = dishName.map { it.isNotEmpty() }.stateIn(
        viewModelScope,
        SharingStarted.Lazily, false
    )

    private var _addedDish = MutableStateFlow<DishBase?>(null)
    val addedDish: StateFlow<DishBase?> = _addedDish // This signals successful save of DishBase, not necessarily ingredients.
                                                    // Consider a different StateFlow for overall success/navigation.

    fun onAddIngredientClicked() {
        val currentName = newIngredientName.value.trim()
        if (currentName.isEmpty()) return

        val productToUse = selectedSuggestedProduct.value
        if (productToUse != null && productToUse.name == currentName) {
            // Existing ingredient selected from suggestions
            // Assuming productToUse.priceAfterWasteAndTax and productToUse.mainUnit are available
            val pricePerOrigUnit = productToUse.price // Or priceAfterWasteAndTax if available and appropriate
            val newDishIngredient = DishIngredient(
                productId = productToUse.id,
                productName = productToUse.name,
                originalProductPricePerUnit = pricePerOrigUnit,
                originalProductUnit = productToUse.mainUnit,
                dishQuantity = MutableStateFlow("1"),
                dishUnit = MutableStateFlow(productToUse.mainUnit) // Default dish unit to product's main unit
            )
            _ingredients.update { it + newDishIngredient }
            recalculateCosts() // Recalculate after adding
            newIngredientName.value = ""
            selectedSuggestedProduct.value = null
            _suggestedIngredients.value = emptyList()
        } else {
            // No suggestion selected or name was manually changed after selection:
            // Treat as a new ingredient creation trigger
            _showNewIngredientDialog.value = true
            // newIngredientName is already set for the dialog's name field
        }
    }

    fun onSaveNewIngredient() {
        val name = newIngredientName.value.trim()
        val price = newIngredientPurchasePrice.value.toDoubleOrNull()
        val unit = newIngredientPurchaseUnit.value
        val waste = newIngredientWastePercentage.value.toDoubleOrNull() ?: 0.0

        if (name.isEmpty() || price == null || price <= 0 || unit.isEmpty()) {
            // TODO: Show some error message to user
            Log.e("CreateDishVM", "Validation failed for new ingredient.")
            return
        }

        viewModelScope.launch(dispatchers.ioDispatcher) {
            val newProduct = ProductBase(
                name = name,
                price = price,
                lastPrice = price,
                mainUnit = unit,
                tax = 0.0, // Default tax, consider making this configurable
                waste = waste
            )
            try {
                val newProductId = productRepository.addProduct(newProduct) // Assuming this returns Long
                if (newProductId > 0) {
                    // For newly added product, originalProductPricePerUnit is the purchase price.
                    // Waste is not factored in here yet for the original price, but could be a refinement.
                    val newDishIngredient = DishIngredient(
                        productId = newProductId,
                        productName = name,
                        originalProductPricePerUnit = price, // price is newIngredientPurchasePrice.value
                        originalProductUnit = unit,          // unit is newIngredientPurchaseUnit.value
                        dishQuantity = MutableStateFlow("1"),
                        dishUnit = MutableStateFlow(unit), // Default dish unit to the purchase unit
                        purchasePrice = price,
                        purchaseUnit = unit
                    )
                    _ingredients.update { it + newDishIngredient }
                    recalculateCosts() // Recalculate after saving and adding
                    analyticsRepository.logEvent(Constants.Analytics.PRODUCT_CREATED_DURING_DISH_CREATION, null)
                    // Reset and close dialog
                    newIngredientName.value = ""
                    newIngredientPurchasePrice.value = ""
                    // newIngredientPurchaseUnit.value = UnitsUtils.getUnitsData().first().abbreviation // Keep last selected or default
                    newIngredientWastePercentage.value = "0"
                    _showNewIngredientDialog.value = false
                    selectedSuggestedProduct.value = null // Clear any lingering selection
                } else {
                    // TODO: Handle error - product not saved
                    Log.e("CreateDishVM", "Failed to save new product, ID was not positive.")
                }
            } catch (e: Exception) {
                // TODO: Handle error - exception during save
                Log.e("CreateDishVM", "Exception while saving new product: ${e.message}")
            }
        }
    }

    fun onDismissNewIngredientDialog() {
        _showNewIngredientDialog.value = false
        // Optionally clear fields, or keep them if user might reopen
        newIngredientPurchasePrice.value = ""
        newIngredientWastePercentage.value = "0"
        // Keep newIngredientName as it might have been what the user intended to type
    }


    // Function to remove an ingredient from the _ingredients list
    fun onRemoveIngredient(ingredientToRemove: DishIngredient) {
        _ingredients.update { currentIngredients ->
            currentIngredients.filterNot { it.tempClientId == ingredientToRemove.tempClientId }
        }
        recalculateCosts() // Recalculate after removing
    }

    // Functions to update dish quantity/unit
    fun updateDishIngredientQuantity(item: DishIngredient, newQuantity: String) {
        item.dishQuantity.value = newQuantity
        recalculateCosts() // Recalculate on quantity change
    }

    fun updateDishIngredientUnit(item: DishIngredient, newUnit: String) {
        item.dishUnit.value = newUnit
        recalculateCosts() // Recalculate on unit change
    }

    private fun recalculateCosts() {
        var currentFoodCost = 0.0
        _ingredients.value.forEach { ingredient ->
            currentFoodCost += getCostOfDishIngredient(ingredient) ?: 0.0
            // If getCostOfDishIngredient returns null, its cost is treated as 0.0 for the sum.
            // Error is already logged in getCostOfDishIngredient.
        }
        _foodCost.value = currentFoodCost

        val marginPercentage = margin.value.toDoubleOrNull() ?: 0.0
        val taxPercentage = tax.value.toDoubleOrNull() ?: 0.0

        // Logic adapted from DishDomain, ensure consistency
        // DishDomain: val priceWithMargin = foodCost * marginPercent / 100
        // This implies marginPercent is the final multiplier (e.g. 300 for 300% markup, so price is 3x foodcost)
        // If margin is meant as "profit margin on top of cost" (e.g. 200% means price is foodcost + 2*foodcost = 3*foodcost)
        // then it should be foodCost * (1 + marginPercentage / 100)
        // Sticking to current DishDomain logic:
        val priceWithMargin = if (marginPercentage > 0) _foodCost.value * (marginPercentage / 100.0) else _foodCost.value
        // If margin is 0, priceWithMargin should be foodCost, not 0. Adjusted above.

        val amountOfTax = priceWithMargin * (taxPercentage / 100.0)
        _finalPrice.value = priceWithMargin + amountOfTax
    }


    fun getCostOfDishIngredient(ingredient: DishIngredient): Double? {
        val dishQuantityValue = ingredient.dishQuantity.value.toDoubleOrNull() ?: return 0.0 // Or null if error
        val dishUnitValue = ingredient.dishUnit.value

        if (dishQuantityValue == 0.0) return 0.0

        if (dishUnitValue == ingredient.originalProductUnit) {
            return ingredient.originalProductPricePerUnit * dishQuantityValue
        } else {
            val convertedQuantity = UnitsUtils.convertUnits(
                quantity = dishQuantityValue,
                sourceUnit = dishUnitValue,
                targetUnit = ingredient.originalProductUnit
            )
            return if (convertedQuantity != null) {
                ingredient.originalProductPricePerUnit * convertedQuantity
            } else {
                Log.w("CreateDishVM", "Cannot convert ${ingredient.productName} from $dishUnitValue to ${ingredient.originalProductUnit}")
                null // Or return 0.0 and log error, indicating cost cannot be calculated
            }
        }
    }

    // Function to handle suggestion selection
    fun onSuggestionSelected(product: ProductDomain) {
        newIngredientName.value = product.name
        selectedSuggestedProduct.value = product // Track the selected product
        _suggestedIngredients.value = emptyList()
    }

    private fun addDish(dish: DishBase) {
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.addDish(dish)
        }
    }

    fun resetAddedDish() {
        _addedDish.value = null
    }

    fun addDish() {
        if (margin.value.isEmpty()) margin.value = Constants.BASIC_MARGIN.toString()
        if (tax.value.isEmpty()) tax.value = Constants.BASIC_TAX.toString()
        val dish = DishBase(
            0,
            dishName.value,
            margin.value.toDoubleOrNull() ?: Constants.BASIC_MARGIN.toDouble(),
            tax.value.toDoubleOrNull() ?: Constants.BASIC_TAX.toDouble(),
            recipeId = null
        )
        addDish(dish)
        analyticsRepository.logEvent(Constants.Analytics.DISH_CREATED, null)
        _addedDish.value = dish // Signal that the DishBase itself was set for addition.
                                // Actual DB operation happens below.

        viewModelScope.launch(dispatchers.ioDispatcher) {
            try {
                val newDishId = dishRepository.addDish(dish) // Assuming this returns the new dishId: Long
                if (newDishId > 0) {
                    _ingredients.value.forEach { dishIngredient ->
                        val productDish = ProductDish(
                            productDishId = 0, // Auto-generated
                            dishId = newDishId,
                            productId = dishIngredient.productId,
                            quantity = dishIngredient.dishQuantity.value.toDoubleOrNull() ?: 0.0,
                            quantityUnit = dishIngredient.dishUnit.value
                        )
                        productRepository.addProductDish(productDish)
                    }
                    analyticsRepository.logEvent(Constants.Analytics.DISH_CREATED, null)
                    Log.i("CreateDishVM", "Dish and its ingredients saved successfully. Dish ID: $newDishId")
                    // Signal overall success to UI if needed (e.g., for navigation or clearing form)
                    // For now, _addedDish.value = dish (from above) might be sufficient if UI observes it for Snackbar.
                    // Consider clearing form here:
                    dishName.value = ""
                    _ingredients.value = emptyList()
                    margin.value = sharedPreferences.defaultMargin.first() // Reset to default
                    tax.value = sharedPreferences.defaultTax.first()       // Reset to default
                    recalculateCosts() // Update costs to zero
                    // _addedDish.value = dish // already set, if this is the signal.
                                        // if a new signal is needed:
                                        // _overallSaveSuccess.value = true
                } else {
                    Log.e("CreateDishVM", "Failed to save dish, newDishId was not positive.")
                    // TODO: Signal error to UI
                }
            } catch (e: Exception) {
                Log.e("CreateDishVM", "Error saving dish and ingredients: ${e.message}", e)
                // TODO: Signal error to UI
            }
        }
    }
}
