package com.example.foodcostcalc

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.foodcostcalc.fragments.*
import com.example.foodcostcalc.model.Product
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {


    lateinit var toolbar: ActionBar
    val productsFragment = Products.newInstance()
    val dishesFragment = Dishes.newInstance()
    val addFragment = Add.newInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




    /** initialize ui with viewmodel*/
    val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)


    /** Open Fragment */
        fun openFragment(fragment: Fragment) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.addToBackStack(fragment.tag)
            transaction.commit()
        }
        /**Navigation menu */
        val menuNavigationClickListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_products -> {
                        toolbar.title = "Products"
                        openFragment(productsFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_dishes -> {
                        toolbar.title = "Dishes"
                        openFragment(dishesFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_add -> {
                        toolbar.title = "Add"
                        openFragment(addFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }


        toolbar = supportActionBar!!
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(menuNavigationClickListener)




    }





}