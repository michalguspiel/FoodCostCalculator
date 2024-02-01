package com.erdees.foodcostcalc.ui.fragments.dishesFragment.addProductToDishDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.entities.Dish
import com.erdees.foodcostcalc.entities.HalfProductIncludedInDish
import com.erdees.foodcostcalc.entities.HalfProduct
import com.erdees.foodcostcalc.entities.ProductIncluded
import com.erdees.foodcostcalc.entities.Product
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils.changeUnitList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddProductToDishFragmentViewModel(application: Application) : AndroidViewModel(application) {

  val readAllHalfProductData: LiveData<List<HalfProduct>>
  val readAllProductData: LiveData<List<Product>>
  val readAllDishData: LiveData<List<Dish>>

  private val productRepository: ProductRepository
  private val halfProductRepository: HalfProductRepository
  private val dishRepository: DishRepository
  private val productIncludedRepository: ProductIncludedRepository
  private val halfProductIncludedInDishRepository: HalfProductIncludedInDishRepository

  val sharedPreferences = SharedPreferences(application)

  init {
    val productDao = AppRoomDataBase.getDatabase(application).productDao()
    val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
    val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
    val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
    val halfProductIncludedInDishDao =
      AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()


    halfProductRepository = HalfProductRepository.getInstance(halfProductDao)
    productRepository = ProductRepository.getInstance(productDao)
    dishRepository = DishRepository.getInstance(dishDao)
    productIncludedRepository = ProductIncludedRepository.getInstance(productIncludedDao)
    halfProductIncludedInDishRepository =
      HalfProductIncludedInDishRepository.getInstance(halfProductIncludedInDishDao)

    readAllHalfProductData = halfProductRepository.readAllData
    readAllProductData = productRepository.readAllData
    readAllDishData = dishRepository.readAllData
  }

  private var metricCondition = true
  private var usaCondition = true

  fun updateUnitsConditions() {
    metricCondition = sharedPreferences.getValueBoolean(Constants.METRIC, true)
    usaCondition = sharedPreferences.getValueBoolean(Constants.IMPERIAL, false)
  }

  private val unitList = arrayListOf<String>() // list for units, to populate spinner

  fun getUnitList(): ArrayList<String> = unitList

  private var chosenUnit: String = ""

  fun chooseUnit(position: Int) {
    chosenUnit = unitList[position]
  }

  private var unitType = ""

  fun updateUnitList() {
    unitList.changeUnitList(
      unitType,
      metricCondition,
      usaCondition
    )
    chosenUnit = unitList.first()
  }

  fun setProductUnitType(position: Int) {
    unitType = UnitsUtils.getUnitType(
      readAllProductData.value?.getOrNull(position)?.unit
    )
  }

  fun setHalfProductUnitType(position: Int) {
    unitType = UnitsUtils.getUnitType(
      readAllHalfProductData.value?.getOrNull(position)?.halfProductUnit
    )
  }

  var productPosition: Int? = null
  var dishPosition: Int? = null

  fun addToDish(
    weight: String?,
    isHalfProductChecked: Boolean
  ): Result {
    if ((weight.isNullOrEmpty() || weight == ".")) return Result.FailureWeight
    if (readAllDishData.value.isNullOrEmpty()) return Result.FailureDish
    if (!isHalfProductChecked && readAllProductData.value.isNullOrEmpty()) return Result.FailureProduct
    if (isHalfProductChecked && readAllHalfProductData.value.isNullOrEmpty()) return Result.FailureProduct

    return if (!isHalfProductChecked) {
      val product = addProductToDish(weight.toDouble())
      Result.SuccessProduct(product.name)
    } else {
      val halfProduct = addHalfProductIncludedInDish(weight.toDouble())
      Result.SuccessHalfProduct(halfProduct.name)
    }
  }

  private fun addProductToDish(product: ProductIncluded) {
    viewModelScope.launch(Dispatchers.IO) {
      productIncludedRepository.addProductToDish(product)
    }
  }

  private fun addProductToDish(
    weight: Double
  ): Product {
    val product = readAllProductData.value?.get(productPosition!!)
    val dish = readAllDishData.value?.get(dishPosition!!)
    val productIncluded = ProductIncluded(
      0,
      product!!,
      dish!!.dishId,
      dish,
      product.productId,
      weight,
      chosenUnit
    )
    addProductToDish(productIncluded)
    return product
  }

  private fun addHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish) {
    viewModelScope.launch(Dispatchers.IO) {
      halfProductIncludedInDishRepository
        .addHalfProductIncludedInDish(halfProductIncludedInDish)
    }
  }

  private fun addHalfProductIncludedInDish(weight: Double): HalfProduct {
    val chosenDish = readAllDishData.value?.get(dishPosition!!)
    val halfProduct = readAllHalfProductData.value?.get(productPosition!!)
    val halfProductIncludedInDish = HalfProductIncludedInDish(
      0,
      chosenDish!!,
      chosenDish.dishId,
      halfProduct!!,
      halfProduct.halfProductId,
      weight,
      chosenUnit
    )
    addHalfProductIncludedInDish(halfProductIncludedInDish)

    return halfProduct
  }

  sealed class Result {
    data class SuccessProduct(val name: String) : Result()
    data class SuccessHalfProduct(val name: String) : Result()
    object FailureWeight : Result()
    object FailureDish : Result()
    object FailureProduct : Result()
  }
}
