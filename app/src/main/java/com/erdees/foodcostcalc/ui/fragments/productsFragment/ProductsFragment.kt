package com.erdees.foodcostcalc.ui.fragments.productsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.databinding.FragmentProductsBinding
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.RecyclerViewAdapterViewModel
import java.util.*


class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModelPassedToRecycler: RecyclerViewAdapterViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        val view: View = binding.root
        val viewModel = ViewModelProvider(this).get(ProductsFragmentViewModel::class.java)
        viewModelPassedToRecycler =
            ViewModelProvider(this).get(RecyclerViewAdapterViewModel::class.java)
        binding.recyclerViewProducts.setHasFixedSize(true)
        setEmptyAdapterToRecyclerView()

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner, { searchWord ->
            viewModel.getProducts().observe(viewLifecycleOwner, { products ->
                val listOfProducts = mutableListOf<ProductModel>()
                products.forEach { listOfProducts.add(it) }
                binding.recyclerViewProducts.adapter =
                    ProductsFragmentRecyclerAdapter(requireActivity(),
                        TAG,
                        listOfProducts.filter {
                            it.name.toLowerCase().contains(searchWord.toLowerCase())
                        } as ArrayList<ProductModel>,
                        childFragmentManager,
                        viewModelPassedToRecycler)
            })
        })
        return view
    }

    /**This must be called immediately in onCreate to avoid error: "E/RecyclerView: No adapterFragment attached; skipping layout" */
    private fun setEmptyAdapterToRecyclerView() {
        binding.recyclerViewProducts.adapter = ProductsFragmentRecyclerAdapter(
            requireActivity(),
            TAG, arrayListOf(), childFragmentManager, viewModelPassedToRecycler
        )
    }

    companion object {
        fun newInstance(): ProductsFragment = ProductsFragment()
        const val TAG = "ProductsFragment"
    }
}