package com.erdees.foodcostcalc.data.basic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded

class BasicDao{
    /** Data access object to basic database  */
    private var mutablePosition: Int? = null
    private val position = MutableLiveData<Int>()

    private var secondMutablePosition: Int? = null
    private val secondPosition = MutableLiveData<Int>()

    /**Flag provides an information if product/dishModel was just deleted and if fragment should close itself */
    private var mutableFlag: Boolean = true
    private val flag = MutableLiveData<Boolean>()

    private var productIncluded: ProductIncluded? = null
    private val productIncludedLive = MutableLiveData<ProductIncluded>()

    private var searchWord: String = ""
    private val searchWordLive = MutableLiveData<String>()

    private var productIncludedInHalfProductModel: ProductIncludedInHalfProductModel? = null
    private val productIncludedInHalfProductLive =
        MutableLiveData<ProductIncludedInHalfProductModel>()

    private var halfProductIncludedModel: HalfProductIncludedInDishModel? = null
    private val halfProductIncludedLive = MutableLiveData<HalfProductIncludedInDishModel>()

    private var dishModelPassedToDialog: DishModel? = null
    private val dishPassedToDialogLive = MutableLiveData<DishModel>()

    private var halfProductModelPassedToDialog: HalfProductModel? = null
    private val halfProductPassedToDialogLive = MutableLiveData<HalfProductModel>()

    private var openAddFlag: Boolean = false
    private val openAddFlagLive = MutableLiveData<Boolean>()

    private var openCreateDishFlag: Boolean = false
    private val openCreateDishFlagLive = MutableLiveData<Boolean>()

    private var openCreateHalfProductFlag: Boolean = false
    private val openCreateHalfProductFlagLive = MutableLiveData<Boolean>()


    init {
        position.value = mutablePosition
        secondPosition.value = secondMutablePosition
        flag.value = mutableFlag
        productIncludedLive.value = productIncluded
        searchWordLive.value = searchWord
        productIncludedInHalfProductLive.value = productIncludedInHalfProductModel
        halfProductIncludedLive.value = halfProductIncludedModel
        openAddFlagLive.value = openAddFlag
        openCreateDishFlagLive.value = openCreateDishFlag
        openCreateHalfProductFlagLive.value = openCreateHalfProductFlag

        dishPassedToDialogLive.value = dishModelPassedToDialog
        halfProductPassedToDialogLive.value = halfProductModelPassedToDialog
    }



    fun setOpenAddFlag(boolean: Boolean){
        openAddFlag = boolean
        openAddFlagLive.value = openAddFlag
    }

    fun observeOpenAddFlag() = openAddFlagLive as LiveData<Boolean>

    fun setOpenCreateDishFlag(boolean: Boolean){
        openCreateDishFlag = boolean
        openCreateDishFlagLive.value = openCreateDishFlag
    }

    fun observeOpenCreateDishFlag() = openCreateDishFlagLive as LiveData<Boolean>

    fun setOpenCreateHalfProductFlag(boolean: Boolean) {
        openCreateHalfProductFlag = boolean
        openCreateHalfProductFlagLive.value = openCreateHalfProductFlag
    }

    fun observeOpenCreateHalfProductFlag() = openCreateHalfProductFlagLive as LiveData<Boolean>


    fun passDishToDialog(dishModel: DishModel) {
        dishModelPassedToDialog = dishModel
        dishPassedToDialogLive.value = dishModelPassedToDialog
    }

    fun getDishToDialog() = dishPassedToDialogLive as LiveData<DishModel>

    fun passHalfProductToDialog(halfProductModel: HalfProductModel) {
        halfProductModelPassedToDialog = halfProductModel
        halfProductPassedToDialogLive.value = halfProductModelPassedToDialog
    }

    fun getHalfProductToDialog() = halfProductPassedToDialogLive as LiveData<HalfProductModel>


    fun setProductIncludedInHalfProduct(productModel: ProductIncludedInHalfProductModel) {
        productIncludedInHalfProductModel = productModel
        productIncludedInHalfProductLive.value = productIncludedInHalfProductModel
    }

    fun getProductIncludedInHalfProduct() =
        productIncludedInHalfProductLive as LiveData<ProductIncludedInHalfProductModel>


    fun searchFor(word: String) {
        searchWord = word
        searchWordLive.value = searchWord
    }

    fun getWhatToSearchFor() = searchWordLive as LiveData<String>

    fun setProductIncluded(product: ProductIncluded) {
        productIncluded = product
        productIncludedLive.value = productIncluded
    }

    fun getProductIncluded() = productIncludedLive as LiveData<ProductIncluded>


    fun setHalfProductIncluded(halfProductModel: HalfProductIncludedInDishModel) {
        halfProductIncludedModel = halfProductModel
        halfProductIncludedLive.value = halfProductIncludedModel
    }

    fun getHalfProductIncluded() =
        halfProductIncludedLive as LiveData<HalfProductIncludedInDishModel>


    fun setPosition(pos: Int) {
        mutablePosition = pos
        position.value = mutablePosition
    }

    fun getPosition() = position as LiveData<Int>

    fun setFlag(boolean: Boolean) {
        mutableFlag = boolean
        flag.value = mutableFlag
    }

    fun getFlag() = flag as LiveData<Boolean>




}