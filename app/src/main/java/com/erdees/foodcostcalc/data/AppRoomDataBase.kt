package com.erdees.foodcostcalc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.erdees.foodcostcalc.data.db.dao.dish.DishDao
import com.erdees.foodcostcalc.data.db.dao.dish.HalfProductDishDao
import com.erdees.foodcostcalc.data.db.dao.dish.ProductDishDao
import com.erdees.foodcostcalc.data.db.dao.halfproduct.HalfProductDao
import com.erdees.foodcostcalc.data.db.dao.halfproduct.ProductHalfProductDao
import com.erdees.foodcostcalc.data.db.dao.product.ProductDao
import com.erdees.foodcostcalc.data.db.migrations.Migration_1to2_RefactorDatabase
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.model.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.data.model.associations.ProductDish
import com.erdees.foodcostcalc.data.model.associations.ProductHalfProduct
import java.io.File

@Database(
  entities = [
    ProductBase::class,
    DishBase::class,
    HalfProductBase::class,

    ProductDish::class,
    ProductHalfProduct::class,
    HalfProductDish::class,
  ],
  version = 2, exportSchema = true,
  views = [],

  )
abstract class AppRoomDataBase : RoomDatabase() {

  abstract fun productDao(): ProductDao
  abstract fun dishDao(): DishDao
  abstract fun halfProductDao(): HalfProductDao
  abstract fun productDishDao(): ProductDishDao
  abstract fun halfProductDishDao(): HalfProductDishDao
  abstract fun productHalfProductDao(): ProductHalfProductDao

  /**Singleton of database.*/
  companion object {

    private const val name = "product_database"

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
          name
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
          name
        )
          .addMigrations(*migrations())
          .createFromFile(file)
          .build()
        INSTANCE = instance
      }
    }

  }
}
