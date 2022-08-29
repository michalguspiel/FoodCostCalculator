package com.erdees.foodcostcalc.data.basic

class BasicRepository(private val basicDao: BasicDao) {

  fun getWhatToSearchFor() = basicDao.getWhatToSearchFor()

  fun searchFor(word: String) = basicDao.searchFor(word)

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
