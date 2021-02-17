package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodcostcalc.model.ProductIncluded

class BasicDao{
    /** Data access object to basic database  */
    private var mutablePosition: Int? = null
    private val position = MutableLiveData<Int>()

    private var secondMutablePosition: Int? = null
    private val secondPosition = MutableLiveData<Int>()

    private var mutableFlag: Boolean = true
    private val flag = MutableLiveData<Boolean>()

    private var productIncluded: ProductIncluded? = null
    private val productIncludedLive = MutableLiveData<ProductIncluded>()

    private var searchWord: String = ""
    private val searchWordLive = MutableLiveData<String>()


    init {
        position.value = mutablePosition
        secondPosition.value = secondMutablePosition
        flag.value = mutableFlag
        productIncludedLive.value = productIncluded
        searchWordLive.value = searchWord
    }

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