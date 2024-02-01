package com.erdees.foodcostcalc.ui.activities.mainActivity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.ActivityMainBinding
import com.erdees.foodcostcalc.ui.activities.onlineDataActivity.OnlineDataActivity
import com.erdees.foodcostcalc.ui.fragments.addFragment.AddFragment
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.DishesFragment
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.addProductToDishDialogFragment.AddProductToDishFragment
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.createDishDialogFragment.CreateDishFragment
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.HalfProductsFragment
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment.AddProductToHalfProductFragment
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.createHalfProductDialogFragment.CreateHalfProductFragment
import com.erdees.foodcostcalc.ui.fragments.productsFragment.ProductsFragment
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SettingsFragment
import com.erdees.foodcostcalc.utils.CallbackListener
import com.erdees.foodcostcalc.utils.ViewUtils.hideKeyboard
import com.erdees.foodcostcalc.utils.ViewUtils.makeGone
import com.erdees.foodcostcalc.utils.ViewUtils.makeVisible
import com.erdees.foodcostcalc.utils.ViewUtils.uncheckAllItems
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
  private lateinit var viewBinding: ActivityMainBinding
  private lateinit var toggle: ActionBarDrawerToggle
  private lateinit var viewModel: MainActivityViewModel

  /**Fragments instances*/
  private val productsFragment = ProductsFragment.newInstance()
  private val dishesFragment = DishesFragment.newInstance()
  private val addFragment = AddFragment.newInstance()
  private val settingsFragment = SettingsFragment.newInstance()
  private val halfProductsFragment = HalfProductsFragment.newInstance()

  /**Hide everything on toolbar but side menu button. */
  private fun hideSearchToolbar() {
    viewBinding.content.customToolbar.searchButton.makeGone()
    viewBinding.content.customToolbar.searchTextField.makeGone()
    viewBinding.content.customToolbar.searchBack.makeGone()
    viewBinding.content.customToolbar.sideMenuButton.makeVisible()
  }

  /**Show search field and search button on toolbar. */
  fun setSearchToolbar() {
    viewBinding.content.customToolbar.toolBarTitle.text = ""
    viewBinding.content.customToolbar.toolBarTitle.makeGone()
    viewBinding.content.customToolbar.searchButton.makeVisible()
    viewBinding.content.customToolbar.searchTextField.makeVisible()
    viewBinding.content.customToolbar.searchTextField.hint = getString(R.string.search_by_name)
  }

  fun openAdd() {
    replaceFragment(addFragment, AddFragment.TAG)
    viewBinding.content.navigationView.uncheckAllItems()
    hideSearchToolbar()
  }

  fun setToolBarTitle(text: String) {
    viewBinding.content.customToolbar.toolBarTitle.makeVisible()
    viewBinding.content.customToolbar.toolBarTitle.text = text
  }

  private fun checkIfSearchToolIsUsed(): Boolean {
    return viewBinding.content.customToolbar.searchTextField.isVisible && viewBinding.content.customToolbar.searchTextField.text.isNotEmpty()
  }

  override fun onBackPressed() {
    when {
      viewBinding.navView.isVisible -> {
        viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
        return
      }
      checkIfSearchToolIsUsed() -> {
        viewBinding.content.customToolbar.searchBack.performClick()
        return
      }
      supportFragmentManager.backStackEntryCount == 1 -> {
        finish()
      }
      else -> {
        super.onBackPressed()
        viewBinding.content.customToolbar.searchBack.performClick()
        when (supportFragmentManager.fragments.last()) { // to setup correct toolbar
          addFragment, settingsFragment -> {
            hideSearchToolbar()
            viewBinding.content.navigationView.uncheckAllItems()
          }
          dishesFragment -> {
            setSearchToolbar()
            viewBinding.content.navigationView.selectedItemId = R.id.navigation_dishes
          }
          halfProductsFragment -> {
            setSearchToolbar()
            viewBinding.content.navigationView.selectedItemId =
              R.id.navigation_half_products
          }
          productsFragment -> {
            setSearchToolbar()
            viewBinding.content.navigationView.selectedItemId = R.id.navigation_products
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
    clearSearchBar(fragment)
  }

  /**
   * When switching to products, half products or dishes fragments, we need to clear search bar.
   * */
  private fun clearSearchBar(fragment : Fragment){
    if (fragment == productsFragment ||
      fragment == dishesFragment ||
      fragment == halfProductsFragment
    ) viewBinding.content.customToolbar.searchBack.performClick() // to clear search while switching fragments.
  }

  private fun openDialog(dialog: DialogFragment) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.addToBackStack(dialog.tag)
    dialog.show(transaction, dialog.tag)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = ActivityMainBinding.inflate(layoutInflater)

    productsFragment.callbackListener = object : CallbackListener {
      override fun callback() {
        openAdd()
      }
    }
    dishesFragment.callbackListener = object : CallbackListener {
      override fun callback() {
        openDialog(CreateDishFragment(viewBinding.drawerLayout))
      }
    }
    halfProductsFragment.callbackListener = object : CallbackListener {
      override fun callback() {
        openDialog(CreateHalfProductFragment(viewBinding.drawerLayout))
      }
    }

    val view = viewBinding.root
    setContentView(view)
    MobileAds.initialize(this) {}

    viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
    viewBinding.content.customToolbar.searchTextField.setOnFocusChangeListener { _, hasFocus ->
      if (!hasFocus) viewBinding.drawerLayout.hideKeyboard()
    }

    viewBinding.content.customToolbar.searchBack.visibility = View.GONE

    viewBinding.content.customToolbar.sideMenuButton.setOnClickListener {
      viewBinding.drawerLayout.open()
      viewBinding.drawerLayout.hideKeyboard()
    }
    viewBinding.content.customToolbar.searchButton.setOnClickListener {
      viewModel.searchFor(viewBinding.content.customToolbar.searchTextField.text.toString())
      viewBinding.content.customToolbar.sideMenuButton.visibility = View.INVISIBLE
      viewBinding.content.customToolbar.sideMenuButton.isEnabled = false
      viewBinding.content.customToolbar.searchBack.visibility = View.VISIBLE
      viewBinding.drawerLayout.hideKeyboard()
    }
    viewBinding.content.customToolbar.searchBack.setOnClickListener {
      viewModel.searchFor("")
      viewBinding.content.customToolbar.searchTextField.text.clear()
      viewBinding.content.customToolbar.searchBack.visibility = View.GONE
      viewBinding.content.customToolbar.sideMenuButton.visibility = View.VISIBLE
      viewBinding.content.customToolbar.sideMenuButton.isEnabled = true
      viewBinding.drawerLayout.hideKeyboard()
    }

    /**Side drawer menu */
    toggle = ActionBarDrawerToggle(this, viewBinding.drawerLayout, 0, 0)
    viewBinding.drawerLayout.addDrawerListener(toggle)

    val sideNavigationClickListener =
      NavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
        when (item.itemId) {
          R.id.nav_add_product -> {
            openAdd()
          }

          R.id.nav_create_new_dish -> {
            openDialog(CreateDishFragment(viewBinding.drawerLayout))
          }

          R.id.nav_add_product_to_dish -> {
            AddProductToDishFragment.dishPassedFromAdapter = null
            openDialog(AddProductToDishFragment())
          }

          R.id.nav_personalize -> {
            replaceFragment(settingsFragment, SettingsFragment.TAG)
            viewBinding.content.navigationView.uncheckAllItems()
            hideSearchToolbar()
          }

          R.id.nav_create_half_product -> {
            openDialog(CreateHalfProductFragment(viewBinding.drawerLayout))
          }

          R.id.nav_add_product_to_half_product -> {
            AddProductToHalfProductFragment.passedHalfProduct = null
            openDialog(AddProductToHalfProductFragment())
          }

          R.id.nav_online_data -> {
            val intent = Intent(this, OnlineDataActivity::class.java)
            startActivity(intent, savedInstanceState)
          }
        }
        viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
        return@OnNavigationItemSelectedListener true
      }

    /**Bottom Navigation menu */
    viewBinding.content.navigationView.setOnItemSelectedListener { item ->
        when (item.itemId) {
          R.id.navigation_products -> {
            replaceFragment(productsFragment, ProductsFragment.TAG)
            setSearchToolbar()
          }

          R.id.navigation_dishes -> {
            replaceFragment(dishesFragment, DishesFragment.TAG)
            setSearchToolbar()
          }

          R.id.navigation_half_products -> {
            replaceFragment(halfProductsFragment, HalfProductsFragment.TAG)
            setSearchToolbar()
          }
        }
      return@setOnItemSelectedListener true
      }
    replaceFragment(productsFragment, ProductsFragment.TAG)

    viewBinding.navView.setNavigationItemSelectedListener(sideNavigationClickListener)
  }

  override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
    toggle.syncState()
    super.onPostCreate(savedInstanceState, persistentState)
  }
}
