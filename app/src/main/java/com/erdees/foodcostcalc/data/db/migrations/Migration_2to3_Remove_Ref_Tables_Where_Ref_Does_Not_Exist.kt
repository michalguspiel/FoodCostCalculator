package com.erdees.foodcostcalc.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * During update from version 1 to 2, the database was not properly migrated.
 *
 * In version 1 users could have removed products, but they still existed in half products or dishes.
 * In version 1 users could have removed half products, but they still existed in dishes.
 *
 * Version 2 doesn't allow for this, instead of reference tables which contained embedded objects like:
 * ProductIncluded, ProductIncludedInHalfProduct and HalfProductIncludedInDish
 * we now have Product_Dish, Product_HalfProduct and HalfProduct_Dish which are reference tables without embedded objects.
 * */
class Migration_2to3_Remove_Ref_Tables_Where_Ref_Does_Not_Exist : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Remove Product_Dish entries where the productId does not exist in ProductBase
        db.execSQL(
            "DELETE FROM Product_Dish WHERE productId NOT IN (SELECT productId FROM products)"
        )

        // Remove Product_HalfProduct entries where the productId does not exist in ProductBase
        db.execSQL(
            "DELETE FROM Product_HalfProduct WHERE productId NOT IN (SELECT productId FROM products)"
        )

        // Remove HalfProduct_Dish entries where the halfProductId does not exist in HalfProductBase
        db.execSQL(
            "DELETE FROM HalfProduct_Dish WHERE halfProductId NOT IN (SELECT halfProductId FROM HalfProduct)"
        )
    }
}
