package com.erdees.foodcostcalc

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.db.migrations.Migration_1to2_RefactorDatabase
import com.erdees.foodcostcalc.data.db.migrations.Migration_2to3_Remove_Ref_Tables_Where_Ref_Does_Not_Exist
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

    @Test
    @Throws(IOException::class)
    fun testMigration_2_to_3() {
        // Create the database with version 2 schema and insert test data
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL("INSERT INTO Product_Dish (productDishId, productId, dishId, quantity, quantityUnit) VALUES (1, 999, 1, 1.0, 'kg')")
            execSQL("INSERT INTO Product_HalfProduct (productHalfProductId, productId, halfProductId, quantity, quantityUnit, weightPiece) VALUES (1, 999, 1, 1.0, 'kg', 1.0)")
            execSQL("INSERT INTO HalfProduct_Dish (halfProductDishId, halfProductId, dishId, quantity, quantityUnit) VALUES (1, 999, 1, 1.0, 'kg')")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration_2to3_Remove_Ref_Tables_Where_Ref_Does_Not_Exist())

        // Verify that the entries with non-existent foreign keys are removed
        db.apply {
            val cursor1 = query("SELECT * FROM Product_Dish WHERE productId = 999")
            assert(cursor1.count == 0)
            cursor1.close()

            val cursor2 = query("SELECT * FROM Product_HalfProduct WHERE productId = 999")
            assert(cursor2.count == 0)
            cursor2.close()

            val cursor3 = query("SELECT * FROM HalfProduct_Dish WHERE halfProductId = 999")
            assert(cursor3.count == 0)
            cursor3.close()

            close()
        }
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
