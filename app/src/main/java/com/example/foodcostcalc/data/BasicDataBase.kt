package com.example.foodcostcalc.data


/** Basic database is temporary, every time app closes everything from here disappears
 * which is okay because in here we only store information like which product position to delete
 * or safe flag state*/
class BasicDataBase private constructor() {

    var basicDao = BasicDao()
        private set

    /**Singleton object */
    companion object {
        @Volatile private var instance: BasicDataBase? = null
        fun getInstance() =
                instance ?: synchronized(this) {
                    instance
                            ?: BasicDataBase().also { instance = it }
                }
    }
}