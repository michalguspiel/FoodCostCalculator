package com.example.foodcostcalc.data.basic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodcostcalc.model.HalfProductIncludedInDish
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.model.ProductIncludedInHalfProduct

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


    init {
        position.value = mutablePosition
        secondPosition.value = secondMutablePosition
        flag.value = mutableFlag
        productIncludedLive.value = productIncluded
        searchWordLive.value = searchWord
        productIncludedInHalfProductLive.value = productIncludedInHalfProduct
        halfProductIncludedLive.value = halfProductIncluded
    }

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