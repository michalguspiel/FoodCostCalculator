package com.example.foodcostcalc.model

data class Dish(val name: String){

    var productsIncluded: MutableList<Product> = mutableListOf()
    var weightList: MutableList<Double> = mutableListOf()


    fun getPairs(): MutableList<Pair<Product,Double>> {
    var result = mutableListOf<Pair<Product,Double>>()
       for (eachProduct in productsIncluded.indices){
           result.add(Pair(productsIncluded[eachProduct],weightList[eachProduct]))
       }
       return result
    }


    fun getPrice():Double {
        var result = 0.0
        for(eachProduct in productsIncluded.indices){
            result += (productsIncluded[eachProduct].properPrice * weightList[eachProduct])
        }
        return result
    }



    override fun toString(): String {
        val resultRounded = String.format("%.2f",getPrice())
        return "$name includes: ${productsIncluded.map{it.name}.joinToString { "$it " }}, total price : $resultRounded"
    }



}