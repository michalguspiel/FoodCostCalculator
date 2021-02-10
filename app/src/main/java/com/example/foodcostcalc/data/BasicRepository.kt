package com.example.foodcostcalc.data

import com.example.foodcostcalc.model.ProductIncluded

class BasicRepository(private val basicDao: BasicDao) {

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

    fun setSecondPosition(pos: Int){
        basicDao.setSecondPosition(pos)
    }

    fun getSecondPosition() = basicDao.getSecondPosition()

    fun setFlag(boolean: Boolean) {
        basicDao.setFlag(boolean)
    }

    fun getFlag() = basicDao.getFlag()


    companion object {
        // Singleton instantiation you already know and love
        @Volatile
        private var instance: BasicRepository? = null

        fun getInstance(basicDao: BasicDao) =
            instance ?: synchronized(this) {
                instance
                    ?: BasicRepository(basicDao).also { instance = it }
            }
    }
}