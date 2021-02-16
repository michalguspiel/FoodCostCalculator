package com.example.foodcostcalc

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
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
    private val settingsFragment = Settings.newInstance()
    private val createDishFragment = CreateDish.newInstance()
    lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)



        /** Open Fragment */
        fun openFragment(fragment: Fragment) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.addToBackStack(fragment.tag)
            transaction.commit()
        }

        /** Toolbar  */
        val menuBtn = findViewById<ImageButton>(R.id.side_menu_button)
        val searchBtn = findViewById<ImageButton>(R.id.search_button)
        val searchTextField = findViewById<EditText>(R.id.toolbar_text_field)
        val backBtn = findViewById<ImageButton>(R.id.search_back)
        backBtn.visibility = View.GONE
        menuBtn.setOnClickListener {
            drawerLayout.open()
        }
        searchBtn.setOnClickListener {
            viewModel.searchFor(searchTextField.text.toString())
            menuBtn.visibility = View.GONE
            backBtn.visibility = View.VISIBLE
        }
        backBtn.setOnClickListener {
            viewModel.searchFor("")
            searchTextField.text.clear()
            backBtn.visibility = View.GONE
            menuBtn.visibility = View.VISIBLE
        }

        /**Hide everything on toolbar but side menu button. */
        fun hideSearchToolbar() {
            searchBtn.visibility = View.GONE
            searchTextField.visibility = View.GONE
            backBtn.visibility = View.GONE
            menuBtn.visibility = View.VISIBLE
        }

        /**Show search field and search button on toolbar. */
        fun setSearchToolbar() {
            searchBtn.visibility = View.VISIBLE
            searchTextField.visibility = View.VISIBLE
            searchTextField.hint = "Search by name"
        }

        /**Side drawer menu */
        drawerLayout = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, 0, 0)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val sideNavigationClickListener =
                NavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.nav_add_product -> {
                            openFragment(addFragment)
                            hideSearchToolbar()
                        }
                        R.id.nav_create_new_dish -> {
                            CreateDish().show(supportFragmentManager, CreateDish.TAG)
                        }
                        R.id.nav_add_product_to_dish -> {
                            AddProductToDish().show(supportFragmentManager, AddProductToDish.TAG)
                        }
                        R.id.nav_personalize -> {
                            openFragment(settingsFragment)
                            hideSearchToolbar()
                        }
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
                            setSearchToolbar()
                            return@OnNavigationItemSelectedListener true
                        }
                        R.id.navigation_dishes -> {
                            openFragment(dishesFragment)
                            setSearchToolbar()

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