package com.erdees.foodcostcalc.ui.fragments.productsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.databinding.FragmentProductsBinding
import com.erdees.foodcostcalc.domain.model.product.ProductModel
import java.util.*


class ProductsFragment(private val navigateToAdd : () -> Unit) : Fragment() {

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
        setEmptyAdapterToRecyclerView()

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner) { searchWord ->
          viewModel.getProducts().observe(viewLifecycleOwner) { products ->
            val listOfProducts = mutableListOf<ProductModel>()
            products.forEach { listOfProducts.add(it) }
            binding.recyclerViewProducts.adapter =
              ProductsFragmentRecyclerAdapter(requireActivity(),
                TAG,
                listOfProducts.filter {
                  it.name.lowercase(Locale.getDefault()).contains(searchWord.toLowerCase())
                } as ArrayList<ProductModel>,
                childFragmentManager,
                navigateToAdd = navigateToAdd)
          }
        }
      return view
    }

    /**This must be called immediately in onCreate to avoid error: "E/RecyclerView: No adapterFragment attached; skipping layout" */
    private fun setEmptyAdapterToRecyclerView() {
        binding.recyclerViewProducts.adapter = ProductsFragmentRecyclerAdapter(
            requireActivity(),
            TAG, arrayListOf(), childFragmentManager
        ) {}
    }

    companion object {
        fun newInstance(navigateToAdd: () -> Unit): ProductsFragment = ProductsFragment(navigateToAdd)
        const val TAG = "ProductsFragment"
    }
}
