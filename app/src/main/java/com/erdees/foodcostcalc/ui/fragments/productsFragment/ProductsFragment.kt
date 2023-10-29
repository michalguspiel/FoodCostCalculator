package com.erdees.foodcostcalc.ui.fragments.productsFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.databinding.FragmentProductsBinding
import com.erdees.foodcostcalc.domain.model.product.ProductModel
import com.erdees.foodcostcalc.ui.activities.mainActivity.MainActivity
import com.erdees.foodcostcalc.utils.CallbackListener
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

    //TODO NOT IN THIS REFACTOR: do not use nested .observe
    viewModel.getWhatToSearchFor().observe(viewLifecycleOwner) { searchWord ->
      viewModel.getProducts().observe(viewLifecycleOwner) { products ->
        Log.i(TAG,"Get new products")
        val list = products.filter {
          it.name.lowercase(Locale.getDefault()).contains(searchWord.lowercase())
        } as ArrayList<ProductModel>
        adapter.switchLists(list)
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
