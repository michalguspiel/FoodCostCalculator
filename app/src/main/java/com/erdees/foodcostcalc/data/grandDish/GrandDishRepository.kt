package com.erdees.foodcostcalc.data.grandDish

class GrandDishRepository(private val dao: GrandDishDao) {

    fun getGrandDishes() = dao.getGrandDishes()

    fun getGrandDishById(dishId: Long) = dao.getGrandDishByDishID(dishId)

    companion object {
        @Volatile
        private var instance: GrandDishRepository? = null

        fun getInstance(grandDishDao: GrandDishDao) =
            instance ?: synchronized(this) {
                instance
                    ?: GrandDishRepository(grandDishDao).also { instance = it }
            }
    }

}
