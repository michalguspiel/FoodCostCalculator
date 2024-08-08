package com.erdees.foodcostcalc.ui.fragments.productsFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.erdees.foodcostcalc.databinding.FragmentProductsBinding
import com.erdees.foodcostcalc.data.model.Product
import com.erdees.foodcostcalc.ui.activities.mainActivity.MainActivity
import com.erdees.foodcostcalc.utils.CallbackListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

class ProductsFragment : Fragment() {
  var callbackListener: CallbackListener? = null

  private var _binding: FragmentProductsBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentProductsBinding.inflate(inflater, container, false)
    val view: View = binding.root
    val viewModel = ViewModelProvider(this).get(ProductsFragmentViewModel::class.java)
    binding.recyclerViewProducts.setHasFixedSize(true)
    val adapter = ProductsFragmentRecyclerAdapter(
      requireActivity(),
      TAG, childFragmentManager, navigateToAdd = { callbackListener?.callback() }
    )
    binding.recyclerViewProducts.adapter = adapter

    viewLifecycleOwner.lifecycleScope.launch {
      viewModel.filteredProducts.collect { products ->
        Log.i(TAG, "Get new products")
        adapter.switchLists(products as ArrayList<Product>)
      }
    }
    return view
  }

  override fun onResume() {
    (activity as MainActivity).setSearchToolbar()
    super.onResume()
  }

  companion object {
    fun newInstance(): ProductsFragment = ProductsFragment()
    const val TAG = "ProductsFragment"
  }
}
