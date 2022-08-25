package com.erdees.foodcostcalc.data.halfProductWithProductsIncluded

class HalfProductWithProductsIncludedRepository(private val halfProductWithProductsIncludedDao : HalfProductWithProductsIncludedDao) {


    val readAllData = halfProductWithProductsIncludedDao.getHalfProductsWithProductsIncluded()

    fun getCertainHalfProductWithProductsIncluded(halfProductId: Long)
    = halfProductWithProductsIncludedDao.getCertainHalfProductWithProductsIncluded(halfProductId)




    companion object{
        @Volatile
        private var instance: HalfProductWithProductsIncludedRepository? = null

        fun getInstance(halfProductWithProductsIncludedDao: HalfProductWithProductsIncludedDao) =
            instance ?: synchronized(this){
                instance
                    ?: HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao).also { instance = it }
            }
    }
}
