package com.erdees.foodcostcalc.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.erdees.foodcostcalc.data.dish.DishDao
import com.erdees.foodcostcalc.data.dishWithProductIncluded.DishWithProductIncludedDao
import com.erdees.foodcostcalc.data.grandDish.GrandDishDao
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishDao
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedDao
import com.erdees.foodcostcalc.data.halfproduct.HalfProductDao
import com.erdees.foodcostcalc.data.product.ProductDao
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedDao
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductDao
import com.erdees.foodcostcalc.model.*
import com.erdees.foodcostcalc.viewmodel.DishesViewModel
import java.io.File

@Database(
    entities = [Product::class, Dish::class,
        ProductIncluded::class,
        HalfProduct::class,
        ProductIncludedInHalfProduct::class,
        HalfProductIncludedInDish::class,]
    , version = 1, exportSchema = true
)
abstract class AppRoomDataBase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun dishDao(): DishDao
    abstract fun halfProductDao(): HalfProductDao
    abstract fun productIncludedInHalfProductDao(): ProductIncludedInHalfProductDao
    abstract fun halfProductWithProductsIncludedDao(): HalfProductWithProductsIncludedDao
    abstract fun halfProductIncludedInDishDao() : HalfProductIncludedInDishDao
    abstract fun productIncludedDao() : ProductIncludedDao
    abstract fun dishWithProductIncludedDao() : DishWithProductIncludedDao
    abstract fun grandDishDao() : GrandDishDao

    /**Singleton of database.*/
    companion object {
        @Volatile
        private var INSTANCE: AppRoomDataBase? = null
        fun getDatabase(context: Context): AppRoomDataBase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDataBase::class.java,
                    "product_database"
                ).allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
        fun recreateDatabaseFromFile(context: Context, file : File){
            INSTANCE?.close()
            INSTANCE = null
            synchronized(this){
            val instance = Room.databaseBuilder(context.applicationContext,AppRoomDataBase::class.java,"product_database")
                .createFromFile(file)
                .build()
            INSTANCE = instance
        }
        }

    }

}