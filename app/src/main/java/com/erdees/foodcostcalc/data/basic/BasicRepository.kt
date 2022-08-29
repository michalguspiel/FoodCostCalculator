package com.erdees.foodcostcalc.data.basic

import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductModel
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProductModel

class BasicRepository(private val basicDao: BasicDao) {

  fun passHalfProductToDialog(halfProductModel: HalfProductModel) =
    basicDao.passHalfProductToDialog(halfProductModel)

  fun getHalfProductToDialog() = basicDao.getHalfProductToDialog()

  fun getWhatToSearchFor() = basicDao.getWhatToSearchFor()

  fun searchFor(word: String) = basicDao.searchFor(word)

  fun setPosition(pos: Int) {
    basicDao.setPosition(pos)
  }

  fun setFlag(boolean: Boolean) {
    basicDao.setFlag(boolean)
  }

  fun getFlag() = basicDao.getFlag()


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
