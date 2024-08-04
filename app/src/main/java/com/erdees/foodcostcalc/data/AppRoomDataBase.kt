package com.erdees.foodcostcalc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.erdees.foodcostcalc.data.dbMigrations.Migration_1to2_RefactorDatabase
import com.erdees.foodcostcalc.data.dish.DishDao
import com.erdees.foodcostcalc.data.halfproduct.HalfProductDao
import com.erdees.foodcostcalc.data.product.ProductDao
import com.erdees.foodcostcalc.entities.Dish
import com.erdees.foodcostcalc.entities.HalfProduct
import com.erdees.foodcostcalc.entities.HalfProductDish
import com.erdees.foodcostcalc.entities.Product
import com.erdees.foodcostcalc.entities.ProductDish
import com.erdees.foodcostcalc.entities.ProductHalfProduct
import java.io.File

@Database(
  entities = [
    Product::class,
    Dish::class,
    HalfProduct::class,

    ProductDish::class,
    ProductHalfProduct::class,
    HalfProductDish::class,
  ], version = 2, exportSchema = true
)
abstract class AppRoomDataBase : RoomDatabase() {

  abstract fun productDao(): ProductDao
  abstract fun dishDao(): DishDao
  abstract fun halfProductDao(): HalfProductDao

  /**Singleton of database.*/
  companion object {
    @Volatile
    private var INSTANCE: AppRoomDataBase? = null

    private fun migrations() = arrayOf(
      Migration_1to2_RefactorDatabase(),
    )

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
        )
          .addMigrations(*migrations())
          .allowMainThreadQueries()
          .build()
        INSTANCE = instance
        return instance
      }
    }

    fun recreateDatabaseFromFile(context: Context, file: File) {
      INSTANCE?.close()
      INSTANCE = null
      synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppRoomDataBase::class.java,
          "product_database"
        )
          .createFromFile(file)
          .build()
        INSTANCE = instance
      }
    }

  }
}
