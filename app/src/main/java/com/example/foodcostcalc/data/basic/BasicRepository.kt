package com.example.foodcostcalc.data.basic

import com.example.foodcostcalc.model.HalfProductIncludedInDish
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.model.ProductIncludedInHalfProduct

class BasicRepository(private val basicDao: BasicDao) {

    fun getProductIncludedInHalfProduct() = basicDao.getProductIncludedInHalfProduct()


    fun setProductIncludedInHalfProduct(product: ProductIncludedInHalfProduct){
        basicDao.setProductIncludedInHalfProduct(product)
    }

    fun getWhatToSearchFor()    = basicDao.getWhatToSearchFor()

    fun searchFor(word: String) = basicDao.searchFor(word)

    fun getProductIncluded() = basicDao.getProductIncluded()

    fun setProductIncluded(product: ProductIncluded){
        basicDao.setProductIncluded(product)
    }

    fun setPosition(pos: Int){
        basicDao.setPosition(pos)
    }

    fun getPosition() = basicDao.getPosition()



    fun setFlag(boolean: Boolean) {
        basicDao.setFlag(boolean)
    }

    fun getFlag() = basicDao.getFlag()



    fun setHalfProductIncluded(halfProductIncluded : HalfProductIncludedInDish){
        basicDao.setHalfProductIncluded(halfProductIncluded)
    }

    fun getHalfProductIncluded() = basicDao.getHalfProductIncluded()


    companion object {
        @Volatile
        private var instance: BasicRepository? = null

        fun getInstance(basicDao: BasicDao) =
            instance ?: synchronized(this) {
                instance
                    ?: BasicRepository(basicDao).also { instance = it }
            }
    }
}