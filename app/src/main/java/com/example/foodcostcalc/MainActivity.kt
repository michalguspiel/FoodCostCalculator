package com.example.foodcostcalc

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.fragments.*
import com.example.foodcostcalc.fragments.dialogs.AddProductToDish
import com.example.foodcostcalc.fragments.dialogs.CreateDish
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {


    private val productsFragment = Products.newInstance()
    private val dishesFragment = Dishes.newInstance()
    private val addFragment = Add.newInstance()
    private val createDishFragment = CreateDish.newInstance()
    lateinit var drawerLayout: DrawerLayout


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

        /**Side drawer menu */
        drawerLayout = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(this,drawerLayout,0,0)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val sideNavigationClickListener =
            NavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
                when (item.itemId){
                    R.id.nav_add_product -> {
                        openFragment(addFragment)
                        Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()}
                    R.id.nav_create_new_dish -> {
                        CreateDish().show(supportFragmentManager,CreateDish.TAG)
                        Toast.makeText(this, "Opened", Toast.LENGTH_SHORT).show()
                        }
                    R.id.nav_add_product_to_dish -> {
                        AddProductToDish().show(supportFragmentManager,AddProductToDish.TAG)
                        Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()}
                    R.id.nav_units -> {Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()}
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                return@OnNavigationItemSelectedListener true
            }

        /**Bottom Navigation menu */
        val menuNavigationClickListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_products -> {
                        openFragment(productsFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_dishes -> {
                        openFragment(dishesFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }
        openFragment(productsFragment)
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(menuNavigationClickListener)

        val sideNavigation: NavigationView = findViewById(R.id.nav_view)
        sideNavigation.setNavigationItemSelectedListener(sideNavigationClickListener)
    }





}