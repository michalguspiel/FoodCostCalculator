package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductModel
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.domain.model.product.ProductModel
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.UnitsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductToHalfProductFragmentViewModel(application: Application) :
    AndroidViewModel(application) {

    val readAllHalfProductModelData: LiveData<List<HalfProductModel>>
    val readAllProductModelData: LiveData<List<ProductModel>>

    private val productRepository: ProductRepository
    private val halfProductRepository: HalfProductRepository
    private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val productIncludedInHalfProductDao =
            AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()

        halfProductRepository = HalfProductRepository(halfProductDao)
        productRepository = ProductRepository(productDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository((productIncludedInHalfProductDao))
        readAllHalfProductModelData = halfProductRepository.readAllData
        readAllProductModelData = productRepository.readAllData
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
        val thisHalfProduct = readAllHalfProductModelData.value!![halfProductPosition!!]
        halfProductUnit = thisHalfProduct.halfProductUnit
        isHalfProductPiece = thisHalfProduct.halfProductUnit == "per piece"
        halfProductUnitType = UnitsUtils.getUnitType(thisHalfProduct.halfProductUnit)
    }

    fun updateChosenProductData(position: Int) {
        productPosition = position
        val chosenProduct =
            readAllProductModelData.value?.get(position)
        unitType = UnitsUtils.getUnitType(
            chosenProduct?.unit
        )
        chosenProductName = chosenProduct!!.name
        isProductPiece = readAllProductModelData.value!![productPosition!!].unit == "per piece"
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
            readAllHalfProductModelData.value?.get(
                halfProductPosition!!
            )
        val chosenProduct =
            readAllProductModelData.value?.get(productPosition!!)
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
