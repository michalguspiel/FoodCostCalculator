package com.erdees.foodcostcalc.ui.screens.dishes.createDish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CreateDishScreenViewModel : ViewModel(), KoinComponent {

    private val dispatchers: MyDispatchers by inject()
    private val dishRepository: DishRepository by inject()
    private val analyticsRepository: AnalyticsRepository by inject()

    private val sharedPreferences: Preferences by inject()

    var dishName = MutableStateFlow("")
    val margin: MutableStateFlow<String> = MutableStateFlow(Constants.BASIC_MARGIN.toString())
    val tax: MutableStateFlow<String> = MutableStateFlow(Constants.BASIC_TAX.toString())

    init {
        viewModelScope.launch(dispatchers.ioDispatcher) {
            launch { margin.update { sharedPreferences.defaultMargin.first() } }
            launch { tax.update { sharedPreferences.defaultTax.first() } }
        }
    }

    val addButtonEnabled = dishName.map { it.isNotEmpty() }.stateIn(
        viewModelScope,
        SharingStarted.Lazily, false
    )

    private var _addedDish = MutableStateFlow<DishBase?>(null)
    val addedDish: StateFlow<DishBase?> = _addedDish

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
        _addedDish.value = dish
    }
}
