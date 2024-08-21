package com.erdees.foodcostcalc.ui.screens.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.databinding.FragmentProductsBinding
import com.erdees.foodcostcalc.utils.CallbackListener
import kotlinx.coroutines.launch

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
    val viewModel = ViewModelProvider(this)[ProductsFragmentViewModel::class.java]
    binding.recyclerViewProducts.setHasFixedSize(true)
    val adapter = ProductsFragmentRecyclerAdapter(
      requireActivity(),
      TAG, childFragmentManager, navigateToAdd = { callbackListener?.callback() }
    )
    binding.recyclerViewProducts.adapter = adapter

    viewLifecycleOwner.lifecycleScope.launch {
      viewModel.filteredProducts.collect { products ->
        adapter.switchLists(products as ArrayList<ProductBase>)
      }
    }
    return view
  }

  companion object {
    fun newInstance(): ProductsFragment = ProductsFragment()
    const val TAG = "ProductsFragment"
  }
}
