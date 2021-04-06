package com.erdees.foodcostcalc.data.basic

import android.util.Half
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.erdees.foodcostcalc.model.*

class BasicDao{
    /** Data access object to basic database  */
    private var mutablePosition: Int? = null
    private val position = MutableLiveData<Int>()

    private var secondMutablePosition: Int? = null
    private val secondPosition = MutableLiveData<Int>()

    /**Flag provides an information if product/dish was just deleted and if fragment should close itself */
    private var mutableFlag: Boolean = true
    private val flag = MutableLiveData<Boolean>()

    private var productIncluded: ProductIncluded? = null
    private val productIncludedLive = MutableLiveData<ProductIncluded>()

    private var searchWord: String = ""
    private val searchWordLive = MutableLiveData<String>()

    private var productIncludedInHalfProduct: ProductIncludedInHalfProduct? = null
    private val productIncludedInHalfProductLive = MutableLiveData<ProductIncludedInHalfProduct>()

    private var halfProductIncluded : HalfProductIncludedInDish? = null
    private val halfProductIncludedLive = MutableLiveData<HalfProductIncludedInDish>()

    private var dishPassedToDialog : Dish? = null
    private val dishPassedToDialogLive = MutableLiveData<Dish>()

    private var halfProductPassedToDialog : HalfProduct? = null
    private val halfProductPassedToDialogLive = MutableLiveData<HalfProduct>()

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
        productIncludedInHalfProductLive.value = productIncludedInHalfProduct
        halfProductIncludedLive.value = halfProductIncluded
        openAddFlagLive.value = openAddFlag
        openCreateDishFlagLive.value = openCreateDishFlag
        openCreateHalfProductFlagLive.value = openCreateHalfProductFlag

        dishPassedToDialogLive.value = dishPassedToDialog
        halfProductPassedToDialogLive.value = halfProductPassedToDialog
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

    fun setOpenCreateHalfProductFlag(boolean: Boolean){
        openCreateHalfProductFlag = boolean
        openCreateHalfProductFlagLive.value = openCreateHalfProductFlag
    }

    fun observeOpenCreateHalfProductFlag() = openCreateHalfProductFlagLive as LiveData<Boolean>


    fun passDishToDialog(dish: Dish) {
        dishPassedToDialog = dish
        dishPassedToDialogLive.value = dishPassedToDialog
    }
    fun getDishToDialog() = dishPassedToDialogLive as LiveData<Dish>

    fun passHalfProductToDialog(halfProduct: HalfProduct) {
        halfProductPassedToDialog = halfProduct
        halfProductPassedToDialogLive.value = halfProductPassedToDialog
    }
    fun getHalfProductToDialog() = halfProductPassedToDialogLive as LiveData<HalfProduct>


    fun setProductIncludedInHalfProduct(product: ProductIncludedInHalfProduct){
        productIncludedInHalfProduct = product
        productIncludedInHalfProductLive.value = productIncludedInHalfProduct
    }

    fun getProductIncludedInHalfProduct() = productIncludedInHalfProductLive as LiveData<ProductIncludedInHalfProduct>


    fun searchFor(word: String){
        searchWord = word
        searchWordLive.value = searchWord
    }

    fun getWhatToSearchFor() = searchWordLive as LiveData<String>

    fun setProductIncluded(product: ProductIncluded){
        productIncluded = product
        productIncludedLive.value = productIncluded
    }

    fun getProductIncluded() = productIncludedLive as LiveData<ProductIncluded>


    fun setHalfProductIncluded(halfProduct : HalfProductIncludedInDish){
        halfProductIncluded = halfProduct
        halfProductIncludedLive.value = halfProductIncluded
    }

    fun getHalfProductIncluded() = halfProductIncludedLive as LiveData<HalfProductIncludedInDish>


    fun setPosition(pos: Int){
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