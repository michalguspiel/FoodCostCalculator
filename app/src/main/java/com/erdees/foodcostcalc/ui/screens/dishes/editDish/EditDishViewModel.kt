package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishBase
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDish
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDish
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class EditDishScreenInteraction : InteractionType {
  data object EditTax : EditDishScreenInteraction()
  data object EditMargin : EditDishScreenInteraction()
  data object EditTotalPrice : EditDishScreenInteraction()
  data class EditItem(val usedItem: UsedItem) : EditDishScreenInteraction()
}

class EditDishViewModel : ViewModel(), KoinComponent {

  private val dishRepository: DishRepository by inject()

  private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
  val screenState: StateFlow<ScreenState> = _screenState

  fun setInteraction(interaction: InteractionType) {
    _screenState.value = ScreenState.Interaction(interaction)
  }

  fun resetScreenState() {
    _screenState.value = ScreenState.Idle
  }

  private var _dish = MutableStateFlow<DishDomain?>(null)
  val dish: StateFlow<DishDomain?> = _dish

  fun updateItemQuantity(value: String, item: UsedItem) {
    val dish = dish.value ?: return
    when (item) {
      is UsedProductDomain -> {
        val index = dish.products.indexOf(item)
        if (index != -1) {
          val updatedItem = item.copy(quantity = value.toDouble())
          _dish.value = _dish.value?.copy(
            products = dish.products.toMutableList().apply { set(index, updatedItem) })
        }
      }

      is UsedHalfProductDomain -> {
        val index = dish.halfProducts.indexOf(item)
        if (index != -1) {
          val updatedItem = item.copy(quantity = value.toDouble())
          _dish.value = _dish.value?.copy(
            halfProducts = dish.halfProducts.toMutableList().apply { set(index, updatedItem) }
          )
        }
      }
    }
    resetScreenState()
  }

  fun updateDishTax(value: Int) {
    _dish.value = _dish.value?.copy(taxPercent = value.toDouble())
  }

  fun updateDishMargin(value: Int) {
    _dish.value = _dish.value?.copy(marginPercent = value.toDouble())
  }

  private var originalProducts: List<UsedProductDomain> = listOf()
  private var originalHalfProducts: List<UsedHalfProductDomain> = listOf()

  val items: StateFlow<List<UsedItem>> = dish.map {
    val products = it?.products ?: listOf()
    val halfProducts = it?.halfProducts ?: listOf()
    products + halfProducts
  }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())

  fun initializeWith(dishDomain: DishDomain) {
    _dish.value = dishDomain
    originalProducts = dishDomain.products
    originalHalfProducts = dishDomain.halfProducts
  }

  /**
   * Removes item from the temporary list of items. Requires saving to persist.
   *
   * @param item The item to remove.
   * */
  fun removeItem(item: UsedItem) {
    val dish = dish.value ?: return
    when (item) {
      is UsedProductDomain -> _dish.value =
        dish.copy(products = dish.products.filter { it != item })

      is UsedHalfProductDomain -> _dish.value =
        dish.copy(halfProducts = dish.halfProducts.filter { it != item })
    }
  }

  fun deleteDish(dishId: Long) {
    _screenState.value = ScreenState.Loading
    viewModelScope.launch(Dispatchers.IO) {
      try {
        dishRepository.deleteDish(dishId)
        _screenState.value = ScreenState.Success
      } catch (e: Exception) {
        _screenState.value = ScreenState.Error(Error(e.message))
      }
    }
  }

  /**
   * This function is responsible for saving the changes made to a dish.
   * It first sets the screen state to loading and then launches a coroutine on the main thread.
   *
   * It retrieves the original list of products and half-products from the dishDomain.
   * If the dishDomain is null, it defaults to an empty list.
   *
   * It then determines which products and half-products have been removed by filtering out items
   * that are in the original list but not in the current list of products and half-products.
   * These removed items are then mapped to their respective data model representations.
   *
   * Similarly, it determines which products and half-products have been edited by filtering out items
   * that are in the current list but not in the original list. These edited items are also mapped to their respective data model representations.
   *
   * The function then enters a try-catch block where it performs the following operations in the IO context:
   * - It iterates over the list of removed products and half-products and deletes each one from the repository.
   * - It iterates over the list of edited products and half-products and updates each one in the repository.
   * - It updates the dish in the repository. If the dishDomain is null, it throws an exception.
   *
   * If all operations are successful, it sets the screen state to success. If an exception is caught, it sets the screen state to error.
   */
  fun saveDish() {
    val dish = dish.value ?: return
    _screenState.value = ScreenState.Loading
    viewModelScope.launch(Dispatchers.Main) {

      val editedProducts =
        dish.products.filterNot { it in originalProducts }.map { it.toProductDish() }
      val editedHalfProducts =
        dish.halfProducts.filterNot { it in originalHalfProducts }.map { it.toHalfProductDish() }

      val removedProducts = originalProducts.filterNot {
        it.id in dish.products.map { product -> product.id }
      }.map { it.toProductDish() }

      val removedHalfProducts = originalHalfProducts.filterNot {
        it.id in dish.halfProducts.map { halfProduct -> halfProduct.id }
      }.map { it.toHalfProductDish() }

      try {
        withContext(Dispatchers.IO) {

          removedProducts.forEach { dishRepository.deleteProductDish(it) }
          removedHalfProducts.forEach { dishRepository.deleteHalfProductDish(it) }

          editedProducts.forEach { dishRepository.updateProductDish(it) }
          editedHalfProducts.forEach { dishRepository.updateHalfProductDish(it) }

          dishRepository.updateDish(this@EditDishViewModel.dish.value!!.toDishBase()) // Throw and handle if dishDomain is null
        }
        _screenState.value = ScreenState.Success
      } catch (e: Exception) {
        _screenState.value = ScreenState.Error(Error(e.message))
      }
    }
  }
}
