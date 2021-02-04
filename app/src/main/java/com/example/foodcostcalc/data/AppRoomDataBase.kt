package com.example.foodcostcalc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.model.ProductIncluded

@Database(entities = [Product::class, Dish::class, ProductIncluded::class],version = 1, exportSchema = false)
abstract class AppRoomDataBase: RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun dishDao(): DishDao

    companion object{
        @Volatile
private var INSTANCE: AppRoomDataBase? = null
    fun getDatabase(context: Context): AppRoomDataBase{
        val tempInstance = INSTANCE
        if(tempInstance != null){
            return tempInstance
        }
        synchronized(this){
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppRoomDataBase::class.java,
                "product_database"
            ).build()
            INSTANCE = instance
            return instance
        }
    }
    }

}