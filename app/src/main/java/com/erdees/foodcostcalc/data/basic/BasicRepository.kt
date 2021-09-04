package com.erdees.foodcostcalc.data.basic

import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded

class BasicRepository(private val basicDao: BasicDao) {

    fun setOpenAddFlag(boolean: Boolean) = basicDao.setOpenAddFlag(boolean)
    fun observeOpenAddFlag() = basicDao.observeOpenAddFlag()

    fun setOpenCreateDishFlag(boolean: Boolean) = basicDao.setOpenCreateDishFlag(boolean)
    fun observeOpenCreateDishFlag() = basicDao.observeOpenCreateDishFlag()

    fun setOpenCreateHalfProductFlag(boolean: Boolean) = basicDao.setOpenCreateHalfProductFlag(boolean)
    fun observeOpenCreateHalfProductFlag() = basicDao.observeOpenCreateHalfProductFlag()

    fun passDishToDialog(dishModel: DishModel) = basicDao.passDishToDialog(dishModel)

    fun passHalfProductToDialog(halfProductModel: HalfProductModel) =
        basicDao.passHalfProductToDialog(halfProductModel)

    fun getDishToDialog() = basicDao.getDishToDialog()

    fun getHalfProductToDialog() = basicDao.getHalfProductToDialog()

    fun getProductIncludedInHalfProduct() = basicDao.getProductIncludedInHalfProduct()


    fun setProductIncludedInHalfProduct(productModel: ProductIncludedInHalfProductModel) {
        basicDao.setProductIncludedInHalfProduct(productModel)
    }

    fun getWhatToSearchFor() = basicDao.getWhatToSearchFor()

    fun searchFor(word: String) = basicDao.searchFor(word)

    fun getProductIncluded() = basicDao.getProductIncluded()

    fun setProductIncluded(product: ProductIncluded) {
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


    fun setHalfProductIncluded(halfProductIncludedModel: HalfProductIncludedInDishModel) {
        basicDao.setHalfProductIncluded(halfProductIncludedModel)
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