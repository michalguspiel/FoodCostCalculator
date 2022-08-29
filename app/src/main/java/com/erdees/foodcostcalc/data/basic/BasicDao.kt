package com.erdees.foodcostcalc.data.basic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductModel
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.domain.model.product.ProductIncluded

class BasicDao {
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

        halfProductPassedToDialogLive.value = halfProductModelPassedToDialog
    }


    fun setOpenAddFlag(boolean: Boolean) {
        openAddFlag = boolean
        openAddFlagLive.value = openAddFlag
    }

    fun observeOpenAddFlag() = openAddFlagLive as LiveData<Boolean>

    fun setOpenCreateDishFlag(boolean: Boolean) {
        openCreateDishFlag = boolean
        openCreateDishFlagLive.value = openCreateDishFlag
    }

    fun observeOpenCreateDishFlag() = openCreateDishFlagLive as LiveData<Boolean>

    fun setOpenCreateHalfProductFlag(boolean: Boolean) {
        openCreateHalfProductFlag = boolean
        openCreateHalfProductFlagLive.value = openCreateHalfProductFlag
    }

    fun observeOpenCreateHalfProductFlag() = openCreateHalfProductFlagLive as LiveData<Boolean>

    fun passHalfProductToDialog(halfProductModel: HalfProductModel) {
        halfProductModelPassedToDialog = halfProductModel
        halfProductPassedToDialogLive.value = halfProductModelPassedToDialog
    }

    fun getHalfProductToDialog() = halfProductPassedToDialogLive as LiveData<HalfProductModel>

    fun setProductIncludedInHalfProduct(productModel: ProductIncludedInHalfProductModel) {
        productIncludedInHalfProductModel = productModel
        productIncludedInHalfProductLive.value = productIncludedInHalfProductModel
    }

  fun searchFor(word: String) {
        searchWord = word
        searchWordLive.value = searchWord
    }

    fun getWhatToSearchFor() = searchWordLive as LiveData<String>

  fun setPosition(pos: Int) {
        mutablePosition = pos
        position.value = mutablePosition
    }

  fun setFlag(boolean: Boolean) {
        mutableFlag = boolean
        flag.value = mutableFlag
    }

    fun getFlag() = flag as LiveData<Boolean>

}
