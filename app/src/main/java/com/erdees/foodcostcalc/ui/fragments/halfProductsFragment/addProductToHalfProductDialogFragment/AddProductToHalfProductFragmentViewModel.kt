package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.data.model.joined.CompleteHalfProduct
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.utils.UnitsUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddProductToHalfProductFragmentViewModel : ViewModel(), KoinComponent {

  private val preferences: Preferences by inject()
  private val productRepository: ProductRepository by inject()
  private val halfProductRepository: HalfProductRepository by inject()

  val halfProducts: LiveData<List<CompleteHalfProduct>> =
    halfProductRepository.completeHalfProducts.asLiveData()
  val products: LiveData<List<ProductBase>> = productRepository.products.asLiveData()

  var isProductPiece: Boolean = false
  var isHalfProductPiece: Boolean = true

  private var productPosition: Int? = null
  private var halfProductPosition: Int? = null

  private var chosenUnit: String = ""
  private var halfProductUnit = ""
  private var chosenProductName = ""
  private var halfProductUnitType = ""
  private var unitType: String? = null

  fun updateChosenHalfProductData(position: Int) {
    halfProductPosition = position
    val thisHalfProduct = halfProducts.value!![halfProductPosition!!].halfProductBase
    halfProductUnit = thisHalfProduct.halfProductUnit
    isHalfProductPiece = thisHalfProduct.halfProductUnit == "per piece"
    halfProductUnitType = UnitsUtils.getUnitType(thisHalfProduct.halfProductUnit) ?: ""
  }

  fun updateChosenProductData(position: Int) {
    productPosition = position
    val chosenProduct =
      products.value?.get(position)
    unitType = UnitsUtils.getUnitType(
      chosenProduct?.unit
    )
    chosenProductName = chosenProduct!!.name
    isProductPiece = products.value!![productPosition!!].unit == "per piece"
  }

  fun getUnitType(): String? {
    return unitType
  }

  fun getHalfProductUnit(): String {
    return halfProductUnit
  }

  fun getHalfProductUnitType(): String {
    return halfProductUnitType
  }

  fun getChosenProductName(): String {
    return chosenProductName
  }

  fun setUnit(unit: String) {
    chosenUnit = unit
  }

  var metricCondition = preferences.metricUsed
  var imperialCondition = preferences.imperialUsed

  fun addProductToHalfProduct(
    weight: Double,
    pieceWeight: Double
  ) {
    val chosenHalfProduct =
      halfProducts.value?.get(
        halfProductPosition!!
      )
    val chosenProduct =
      products.value?.get(productPosition!!)

    // todo fix
//    addProductIncludedInHalfProduct(
//      ProductIncludedInHalfProduct(
//        0,
//        chosenProduct!!,
//        chosenHalfProduct!!,
//        chosenHalfProduct.halfProductId,
//        weight,
//        chosenUnit,
//        pieceWeight
//      )
//    )
  }
}
