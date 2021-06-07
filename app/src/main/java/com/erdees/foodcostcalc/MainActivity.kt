package com.erdees.foodcostcalc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.ContentView
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
import com.erdees.foodcostcalc.SharedFunctions.hideKeyboard
import com.erdees.foodcostcalc.fragments.*
import com.erdees.foodcostcalc.fragments.dialogs.AddProductToDish
import com.erdees.foodcostcalc.fragments.dialogs.AddProductToHalfProduct
import com.erdees.foodcostcalc.fragments.dialogs.CreateDish
import com.erdees.foodcostcalc.fragments.dialogs.CreateHalfProduct
import com.erdees.foodcostcalc.viewmodel.MainActivityViewModel
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {

    /**Button binders*/
    private lateinit var searchBtn: ImageButton
    private lateinit var menuBtn: ImageButton
    private lateinit var searchTextField: EditText
    private lateinit var backBtn: ImageButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var menuNavigationClickListener: BottomNavigationView.OnNavigationItemSelectedListener
    private lateinit var sideNavigation: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    /**Fragments instances*/
    private val productsFragment = Products.newInstance()
    private val dishesFragment = Dishes.newInstance()
    private val addFragment = Add.newInstance()
    private val settingsFragment = Settings.newInstance()
    private val halfProductsFragment = HalfProducts.newInstance()

    private fun BottomNavigationView.uncheckAllItems() {
        menu.setGroupCheckable(0, true, false)
        for (i in 0 until menu.size()) {
            menu.getItem(i).isChecked = false
        }
        menu.setGroupCheckable(0, true, true)
    }

    /**Hide everything on toolbar but side menu button. */
    private fun hideSearchToolbar() {
        searchBtn.visibility = View.GONE
        searchTextField.visibility = View.GONE
        backBtn.visibility = View.GONE
        menuBtn.visibility = View.VISIBLE
    }

    /**Show search field and search button on toolbar. */
    private fun setSearchToolbar() {
        searchBtn.visibility = View.VISIBLE
        searchTextField.visibility = View.VISIBLE
        searchTextField.hint = "Search by name"
    }

    private fun openAdd() {
        replaceFragment(addFragment, Add.TAG)
        bottomNavigation.uncheckAllItems()
        hideSearchToolbar()
    }


    private fun checkIfSearchToolIsUsed(): Boolean {
        return searchTextField.isVisible && searchTextField.text.isNotEmpty()
    }

    override fun onBackPressed() {
        when {
            sideNavigation.isVisible -> {
                drawerLayout.closeDrawer(GravityCompat.START)
                return
            }
            checkIfSearchToolIsUsed() -> {
                backBtn.performClick()
                return
            }
            supportFragmentManager.backStackEntryCount == 1 -> {
                finish()
            }
            else -> {
                super.onBackPressed()
                backBtn.performClick() // to clear search bar
                when (supportFragmentManager.fragments.last()) { // to setup correct toolbar
                    addFragment -> {
                        hideSearchToolbar()
                        bottomNavigation.uncheckAllItems()
                    }
                    settingsFragment -> {
                        hideSearchToolbar()
                        bottomNavigation.uncheckAllItems()
                    }
                    dishesFragment -> {
                        setSearchToolbar()
                        bottomNavigation.selectedItemId = R.id.navigation_dishes
                    }
                    halfProductsFragment -> {
                        setSearchToolbar()
                        bottomNavigation.selectedItemId = R.id.navigation_half_products
                    }
                    productsFragment -> {
                        setSearchToolbar()
                        bottomNavigation.selectedItemId = R.id.navigation_products
                    }
                }
            }
        }
    }


    private fun replaceFragment(fragment: Fragment, fragmentTag: String) {
        val backStateName = fragment.javaClass.name
        val manager: FragmentManager = supportFragmentManager
        val fragmentPopped = manager.popBackStackImmediate(backStateName, 0)
        if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null) { //if fragment isn't in backStack, create it
            val ft: FragmentTransaction = manager.beginTransaction()
            ft.replace(R.id.container, fragment, fragmentTag)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.addToBackStack(backStateName)
            ft.commit()
        }
        if (fragment == productsFragment ||
            fragment == dishesFragment ||
            fragment == halfProductsFragment
        ) backBtn.performClick() // to clear search while switching fragments.
    }

    private fun openDialog(dialog: DialogFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(dialog.tag)
        dialog.show(transaction, dialog.tag)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        MobileAds.initialize(this){}

        val viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        bottomNavigation = findViewById(R.id.navigationView)

        /** Toolbar  */
        menuBtn = findViewById(R.id.side_menu_button)
        searchBtn = findViewById(R.id.search_button)
        searchTextField = findViewById(R.id.toolbar_text_field)

        searchTextField.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) drawerLayout.hideKeyboard()
        }


        backBtn = findViewById(R.id.search_back)

        backBtn.visibility = View.GONE

        menuBtn.setOnClickListener {
            drawerLayout.open()
            drawerLayout.hideKeyboard()
        }
        searchBtn.setOnClickListener {
            viewModel.searchFor(searchTextField.text.toString())
            menuBtn.visibility = View.INVISIBLE
            menuBtn.isEnabled = false
            backBtn.visibility = View.VISIBLE
            drawerLayout.hideKeyboard()
        }
        backBtn.setOnClickListener {
            viewModel.searchFor("")
            searchTextField.text.clear()
            backBtn.visibility = View.GONE
            menuBtn.visibility = View.VISIBLE
            menuBtn.isEnabled = true
            drawerLayout.hideKeyboard()
        }


        /**Listen to viewmodel signals to open fragments or dialog fragments.*/
        viewModel.observeOpenAddFlag().observe(this, { shouldAddBeOpened ->
            if (shouldAddBeOpened) {
                openAdd()
            }
        })
        viewModel.observeOpenCreateDishFlag().observe(this, { shouldCreateDishBeOpened ->
            if (shouldCreateDishBeOpened) {
                openDialog(CreateDish())
            }
        })
        viewModel.observeOpenCreateHalfProductFlag()
            .observe(this, { shouldCreateHalfProductBeOpened ->
                if (shouldCreateHalfProductBeOpened) {
                    openDialog(CreateHalfProduct())
                }
            })


        /**Side drawer menu */
        val toggle = ActionBarDrawerToggle(this, drawerLayout, 0, 0)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val sideNavigationClickListener =
            NavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.nav_add_product -> {
                        openAdd()
                    }
                    R.id.nav_create_new_dish -> {
                        openDialog(CreateDish())
                    }
                    R.id.nav_add_product_to_dish -> {
                        openDialog(AddProductToDish())
                    }
                    R.id.nav_personalize -> {
                        replaceFragment(settingsFragment, Settings.TAG)
                        bottomNavigation.uncheckAllItems()
                        hideSearchToolbar()
                    }
                    R.id.nav_create_half_product -> {
                        openDialog(CreateHalfProduct())
                    }
                    R.id.nav_add_product_to_half_product -> {
                        openDialog(AddProductToHalfProduct())
                    }
                    R.id.nav_online_data -> {
                        val intent = Intent(this,OnlineDataActivity::class.java)
                        startActivity(intent,savedInstanceState)
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                return@OnNavigationItemSelectedListener true
            }

        /**Bottom Navigation menu */
        menuNavigationClickListener =
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
                    R.id.navigation_half_products -> {
                        replaceFragment(halfProductsFragment, HalfProducts.TAG)
                        setSearchToolbar()
                        return@OnNavigationItemSelectedListener true
                    }
                }
                true
            }
        replaceFragment(productsFragment, Products.TAG)
        bottomNavigation.setOnNavigationItemSelectedListener(menuNavigationClickListener)

        sideNavigation = findViewById(R.id.nav_view)
        sideNavigation.setNavigationItemSelectedListener(sideNavigationClickListener)
    }



}