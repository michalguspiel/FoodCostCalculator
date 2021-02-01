package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product

/** DATA ACCESS OBJECT */
class ProductDao {
    //SIMULATION OF DATABASE
    private val productList = mutableListOf<Product>()
    private val products = MutableLiveData<List<Product>>()
    private val dishList = mutableListOf<Dish>()
    private val dishes = MutableLiveData<List<Dish>>()

    private var mutablePosition: Int? = null
    private val position = MutableLiveData<Int>()

    private var secondMutablePosition: Int? = null
    private val secondPosition = MutableLiveData<Int>()

    private var mutableFlag: Boolean = true
    private val flag = MutableLiveData<Boolean>()



    init {
        // Immediately connect the now empty productList
        // to the MutableLiveData which can be observed
        products.value = productList
        dishes.value   = dishList
        position.value = mutablePosition
        secondPosition.value = secondMutablePosition
        flag.value = mutableFlag
    }


    fun addProduct(product: Product){
        productList.add(product)
        // after adding product update products value
        // which will notify observers
        products.value = productList
    }

    // Casting MutableLiveData to LiveData because its value
    // shouldn't be changed from other classes
    fun getProducts() = products as LiveData<List<Product>>

    fun addDish(dish: Dish){
        dishList.add(dish)
        dishes.value = dishList
    }

    fun getDishes() = dishes as LiveData<List<Dish>>


    fun addProductToDish(dish: Dish, product: Product, weight: Double){
        dish.productsIncluded.add(product)
        dish.weightList.add(weight)
        dishes.value = dishList
    }

    fun editProduct(newProduct: Product, oldProduct: Product){
        productList.remove(oldProduct)
        addProduct(newProduct)

    }

    fun deleteProduct(product: Product){
        productList.remove(product)
        products.value = productList
    }

    fun setPosition(pos: Int){
        mutablePosition = pos
        position.value = mutablePosition
    }

    fun getPosition() = position as LiveData<Int>

    fun setSecondPosition(pos: Int){
        secondMutablePosition = pos
        secondPosition.value = secondMutablePosition
    }

    fun getSecondPosition() = secondPosition as LiveData<Int>

    fun setFlag(boolean: Boolean) {
        mutableFlag = boolean
        flag.value = mutableFlag
    }

    fun getFlag() = flag as LiveData<Boolean>

    fun editDish(dish: Dish,listOfProducts: MutableList<Pair<Product, Double>>) {
        dish.productsIncluded.clear()
        dish.weightList.clear()
        dish.productsIncluded.addAll(listOfProducts.map { it.first })
        dish.weightList.addAll(listOfProducts.map{it.second})
        dishes.value = dishList

    }

    fun deleteDish(dish: Dish){
        dishList.remove(dish)
        dishes.value = dishList
    }


    fun deleteProductFromDish(dish:Dish ,product: Product){
        dish.productsIncluded.remove(product)
        dishes.value = dishList
    }
}
