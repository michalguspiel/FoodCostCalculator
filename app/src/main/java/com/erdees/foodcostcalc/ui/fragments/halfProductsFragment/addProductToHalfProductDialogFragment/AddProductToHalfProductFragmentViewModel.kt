package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.entities.HalfProduct
import com.erdees.foodcostcalc.entities.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.entities.Product
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.UnitsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductToHalfProductFragmentViewModel(application: Application) :
    AndroidViewModel(application) {

    val readAllHalfProductData: LiveData<List<HalfProduct>>
    val readAllProductData: LiveData<List<Product>>

    private val productRepository: ProductRepository
    private val halfProductRepository: HalfProductRepository
    private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val productIncludedInHalfProductDao =
            AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()

        halfProductRepository = HalfProductRepository.getInstance(halfProductDao)
        productRepository = ProductRepository.getInstance(productDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository((productIncludedInHalfProductDao))
        readAllHalfProductData = halfProductRepository.readAllData
        readAllProductData = productRepository.readAllData
    }

    var isProductPiece: Boolean = false
    var isHalfProductPiece: Boolean = true

    private var productPosition: Int? = null
    private var halfProductPosition: Int? = null

    private var chosenUnit: String = ""
    private var halfProductUnit = ""
    private var chosenProductName = ""
    private var halfProductUnitType = ""
    private var unitType = ""

    fun updateChosenHalfProductData(position: Int) {
        halfProductPosition = position
        val thisHalfProduct = readAllHalfProductData.value!![halfProductPosition!!]
        halfProductUnit = thisHalfProduct.halfProductUnit
        isHalfProductPiece = thisHalfProduct.halfProductUnit == "per piece"
        halfProductUnitType = UnitsUtils.getUnitType(thisHalfProduct.halfProductUnit)
    }

    fun updateChosenProductData(position: Int) {
        productPosition = position
        val chosenProduct =
            readAllProductData.value?.get(position)
        unitType = UnitsUtils.getUnitType(
            chosenProduct?.unit
        )
        chosenProductName = chosenProduct!!.name
        isProductPiece = readAllProductData.value!![productPosition!!].unit == "per piece"
    }

    fun getUnitType(): String {
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

    var metricCondition = true
    var usaCondition = true

    val sharedPreferences = SharedPreferences(application)

    fun updateUnitsConditions() {
        metricCondition = sharedPreferences.getValueBoolean(Constants.METRIC, true)
        usaCondition = sharedPreferences.getValueBoolean(Constants.IMPERIAL, false)
    }

    fun addProductToHalfProduct(
        weight: Double,
        pieceWeight: Double
    ) {
        val chosenHalfProduct =
            readAllHalfProductData.value?.get(
                halfProductPosition!!
            )
        val chosenProduct =
            readAllProductData.value?.get(productPosition!!)
        addProductIncludedInHalfProduct(
            ProductIncludedInHalfProduct(
                0,
                chosenProduct!!,
                chosenHalfProduct!!,
                chosenHalfProduct.halfProductId,
                weight,
                chosenUnit,
                pieceWeight
            )
        )
    }

    private fun addProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.addProductIncludedInHalfProduct(
                productIncludedInHalfProduct
            )
        }
    }
}
