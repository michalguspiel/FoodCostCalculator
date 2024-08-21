package com.erdees.foodcostcalc

import com.erdees.foodcostcalc.utils.Utils.getBasicRecipeAsPercentageOfTargetRecipe
import com.erdees.foodcostcalc.utils.Utils.getIngredientForHundredPercentOfRecipe
import org.junit.Assert.assertEquals
import org.junit.Test


class HalfProductBaseFragmentRecyclerAdapterTestsBase {

    @Test
    fun additionIsCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `given I need 2500g of product and basic recipe is for 95g of product, basic recipe percentage should be 3point8`() {
        assertEquals(3.8, getBasicRecipeAsPercentageOfTargetRecipe(2500.0, 95.0), 0.01)
    }

    @Test
    fun `given basic recipe percentage is 3point8 and amount of chicken in recipe is 50grams final quantity of chicken should be 1315`(){
        assertEquals(1315.7,getIngredientForHundredPercentOfRecipe(50.0,3.8),0.1)
    }

    @Test
    fun `Given i pass same amount of target quantity as entry i should recive 100 percent and then same ingredient quantity as given`(){
        assertEquals(100.0,getBasicRecipeAsPercentageOfTargetRecipe(2500.0,2500.0),0.01)
        assertEquals(50.0,getIngredientForHundredPercentOfRecipe(50.0,100.0),0.01)
    }


}
