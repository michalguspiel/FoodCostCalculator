package com.example.foodcostcalc

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.fragments.*
import com.example.foodcostcalc.fragments.dialogs.AddProductToDish
import com.example.foodcostcalc.fragments.dialogs.CreateDish
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {

    lateinit var searchBtn: ImageButton
    lateinit var menuBtn: ImageButton
    lateinit var searchTextField: EditText
    lateinit var backBtn: ImageButton
    lateinit var bottomNavigation: BottomNavigationView
    

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

    override fun onBackPressed() {
        if (searchTextField.isVisible && searchTextField.text.isNotEmpty()) {
            backBtn.performClick()
        }
        else  if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        }
        else {
            super.onBackPressed()
            backBtn.performClick() //clear search bar
            when (supportFragmentManager.fragments.last()) { // to setup correct toolbar
                addFragment -> hideSearchToolbar()
                settingsFragment -> hideSearchToolbar()
                dishesFragment -> {
                    setSearchToolbar()

                }
                productsFragment -> {
                    setSearchToolbar()
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, fragmentTag: String) {
        val backStateName = fragment.javaClass.name
        val manager: FragmentManager = supportFragmentManager
        val fragmentPopped = manager.popBackStackImmediate(backStateName, 0)

        if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null) { //fragment not in back stack, create it.
            val ft: FragmentTransaction = manager.beginTransaction()
            ft.replace(R.id.container, fragment, fragmentTag)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.addToBackStack(backStateName)
            ft.commit()
        }
        if (fragment == productsFragment || fragment == dishesFragment) backBtn.performClick() // to clear search while switching fragments.
    }

    private fun openDialog(dialog: DialogFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(dialog.tag)
        dialog.show(transaction, dialog.tag)
    }


    private val productsFragment = Products.newInstance()
    private val dishesFragment = Dishes.newInstance()
    private val addFragment = Add.newInstance()
    private val settingsFragment = Settings.newInstance()
    private lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)


        bottomNavigation = findViewById(R.id.navigationView)
        /** Toolbar  */
        menuBtn = findViewById(R.id.side_menu_button)
        searchBtn = findViewById(R.id.search_button)
        searchTextField = findViewById(R.id.toolbar_text_field)
        backBtn = findViewById(R.id.search_back)

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


        /**Side drawer menu */
        drawerLayout = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, 0, 0)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val sideNavigationClickListener =
            NavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.nav_add_product -> {
                        replaceFragment(addFragment, Add.TAG)
                        hideSearchToolbar()
                    }
                    R.id.nav_create_new_dish -> {
                        openDialog(CreateDish())
                    }
                    R.id.nav_add_product_to_dish -> {
                        openDialog(AddProductToDish())
                    }
                    R.id.nav_personalize -> {
                        replaceFragment(settingsFragment, Settings.TAG)
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
                        replaceFragment(productsFragment, Products.TAG)
                        setSearchToolbar()
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_dishes -> {
                        replaceFragment(dishesFragment, Dishes.TAG)
                        setSearchToolbar()
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }
        replaceFragment(productsFragment, Products.TAG)
        bottomNavigation.setOnNavigationItemSelectedListener(menuNavigationClickListener)

        val sideNavigation: NavigationView = findViewById(R.id.nav_view)
        sideNavigation.setNavigationItemSelectedListener(sideNavigationClickListener)
    }


}