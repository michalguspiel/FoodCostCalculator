package com.erdees.foodcostcalc.ui.fragments.dishesFragment.addProductToDishDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils.changeUnitList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class AddProductToDishFragmentViewModel(application: Application) : AndroidViewModel(application) {


    val readAllHalfProductModelData: LiveData<List<HalfProductModel>>
    val readAllProductModelData: LiveData<List<ProductModel>>
    val readAllDishModelData: LiveData<List<DishModel>>

    private val productRepository: ProductRepository
    private val halfProductRepository: HalfProductRepository
    private val dishRepository: DishRepository
    private val basicRepository: BasicRepository
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
        val basicDao = BasicDataBase.getInstance().basicDao



        basicRepository = BasicRepository(basicDao)
        halfProductRepository = HalfProductRepository(halfProductDao)
        productRepository = ProductRepository(productDao)
        dishRepository = DishRepository(dishDao)
        productIncludedRepository = ProductIncludedRepository(productIncludedDao)
        halfProductIncludedInDishRepository =
            HalfProductIncludedInDishRepository(halfProductIncludedInDishDao)

        readAllHalfProductModelData = halfProductRepository.readAllData
        readAllProductModelData = productRepository.readAllData
        readAllDishModelData = dishRepository.readAllData

    }

    private var metricCondition = true
    private var usaCondition = true

    fun updateUnitsConditions() {
        metricCondition = sharedPreferences.getValueBoolean(Constants.METRIC, true)
        usaCondition = sharedPreferences.getValueBoolean(Constants.USA, false)
    }

    private val unitList = arrayListOf<String>() // list for units, to populate spinner

    fun getUnitList(): ArrayList<String> = unitList

    var chosenUnit: String = ""

    fun chooseUnit(position: Int) {
        chosenUnit = unitList[position]
    }

    fun updateUnitList() {
        unitList.changeUnitList(
            getUnitType(),
            metricCondition,
            usaCondition
        )
        chosenUnit = unitList.first()
    }

    private var unitType = ""

    fun getUnitType(): String = unitType

    fun setProductUnitType(position: Int) {
        unitType = UnitsUtils.getUnitType(
            readAllProductModelData.value?.get(position)?.unit
        )
    }

    fun setHalfProductUnitType(position: Int) {
        unitType = UnitsUtils.getUnitType(
            readAllHalfProductModelData.value?.get(position)?.halfProductUnit
        )
    }

    var productPosition: Int? = null
    var dishPosition: Int? = null

    fun getDishToDialog() = basicRepository.getDishToDialog()

    fun getHalfProducts() = halfProductRepository.readAllData

    private fun addProductToDish(product: ProductIncluded) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedRepository.addProductToDish(product)
        }
    }

    fun addProductToDish(
        weight: Double
    ): ProductModel {
        val product = readAllProductModelData.value?.get(productPosition!!)
        val dish = readAllDishModelData.value?.get(dishPosition!!)
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


    private fun addHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductIncludedInDishRepository
                .addHalfProductIncludedInDish(halfProductIncludedInDishModel)
        }
    }

    fun addHalfProductIncludedInDish(weight: Double): HalfProductModel {
        val chosenDish = readAllDishModelData.value?.get(dishPosition!!)
        val halfProduct = readAllHalfProductModelData.value?.get(productPosition!!)
        val halfProductIncludedInDish = HalfProductIncludedInDishModel(
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


}