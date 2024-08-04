package com.erdees.foodcostcalc.ui.fragments.dishesFragment.addProductToDishDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.entities.Dish
import com.erdees.foodcostcalc.entities.HalfProduct
import com.erdees.foodcostcalc.entities.Product
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils.changeUnitList

class AddProductToDishFragmentViewModel(application: Application) : AndroidViewModel(application) {

  private val productDao = AppRoomDataBase.getDatabase(application).productDao()
  private val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
  private val dishDao = AppRoomDataBase.getDatabase(application).dishDao()

  private val productRepository: ProductRepository = ProductRepository.getInstance(productDao)
  private val halfProductRepository: HalfProductRepository = HalfProductRepository.getInstance(halfProductDao)
  private val dishRepository: DishRepository = DishRepository.getInstance(dishDao)

  val sharedPreferences = SharedPreferences(application)

  val readAllHalfProductData: LiveData<List<HalfProduct>> = halfProductRepository.readAllData
  val readAllProductData: LiveData<List<Product>> = productRepository.readAllData
  val readAllDishData: LiveData<List<Dish>> = dishRepository.readAllData

  private var metricCondition = true
  private var usaCondition = true

  fun updateUnitsConditions() {
    // TODO, CAN WE JUST INITIALIZE VIEWMODEL WITH THIS?
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

  private fun addProductToDish(
    weight: Double
  ): Product {
    // TODO FIX, THIS IS NOT WORKING
    val product = readAllProductData.value?.get(productPosition!!)
    val dish = readAllDishData.value?.get(dishPosition!!)
//    val productIncluded = ProductIncluded(
//      0,
//      product!!,
//      dish!!.dishId,
//      dish,
//      product.productId,
//      weight,
//      chosenUnit
//    )
//    return product
    return product!!
  }


  private fun addHalfProductIncludedInDish(weight: Double): HalfProduct {

    // TODO FIX, THIS IS NOT WORKING
    val chosenDish = readAllDishData.value?.get(dishPosition!!)
    val halfProduct = readAllHalfProductData.value?.get(productPosition!!)

    return halfProduct!!
  }

  sealed class Result {
    data class SuccessProduct(val name: String) : Result()
    data class SuccessHalfProduct(val name: String) : Result()
    object FailureWeight : Result()
    object FailureDish : Result()
    object FailureProduct : Result()
  }
}
