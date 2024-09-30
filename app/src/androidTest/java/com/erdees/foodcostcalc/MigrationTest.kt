package com.erdees.foodcostcalc

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.db.migrations.Migration_1to2_RefactorDatabase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4ClassRunner::class)
class MigrationTest {
  private lateinit var helper: MigrationTestHelper

  @Before
  fun setUp() {
    helper = MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      AppRoomDataBase::class.java,
      listOf(),
      FrameworkSQLiteOpenHelperFactory()
    )
  }

  @Test
  @Throws(IOException::class)
  fun testMigration_1to2_RefactorDatabase() {
    // Create the database with version 1
    helper.createDatabase(TEST_DB, 1).apply {
      // Insert some data
      execSQL(
        """
        INSERT INTO ProductIncluded (productIncludedId, productOwnerId, dishOwnerId, weight, weightUnit, productId, product_name, pricePerUnit, tax, waste, unit, dishId, dish_name, margin_percent, dish_tax)
        VALUES (1, 1, 1, 100.0, 'g', 1, 'Product Name', 10.0, 5.0, 2.0, 'kg',1 ,'Dish Name', 100.0, 0.0)
    """.trimIndent()
      )
      execSQL(
        """
      INSERT INTO HalfProductIncludedInDish (halfProductOwnerId, dishOwnerId, weight, unit, dishId, dish_name, margin_percent, dish_tax,halfProductId, name, halfProductUnit)
      VALUES (1, 1, 100.0, 'g',1,'Dish Name', 100.0, 0.0, 1, 'Half Product Name', 'kg')
      """.trimIndent()
      )

      // Insert some data into ProductIncludedInHalfProduct
      execSQL(
        """
      INSERT INTO ProductIncludedInHalfProduct (halfProductHostId, weight, weightUnit, weightOfPiece,productId, product_name, pricePerUnit, tax, waste, unit,halfProductId, name, halfProductUnit)
      VALUES (1, 100.0, 'g', 50.0, 1, 'Product Name', 10.0, 5.0, 2.0, 'kg',1, 'Half Product Name', 'kg')
      """.trimIndent()
      )

      close()
    }

    // Migrate the database to version 2
    helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration_1to2_RefactorDatabase())
  }

  companion object {
    private const val TEST_DB = "migration-test"
  }
}
