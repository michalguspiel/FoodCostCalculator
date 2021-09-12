package com.erdees.foodcostcalc.ui.fragments.productsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.RecyclerViewAdapterViewModel
import java.util.*

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class ProductsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModelPassedToRecycler: RecyclerViewAdapterViewModel
    private lateinit var adapterFragment: ProductsFragmentRecyclerAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_products, container, false)
        val viewModel = ViewModelProvider(this).get(ProductsFragmentViewModel::class.java)
        viewModelPassedToRecycler =
            ViewModelProvider(this).get(RecyclerViewAdapterViewModel::class.java)
        recyclerView = view.findViewById(R.id.recycler_view_products)
        recyclerView.setHasFixedSize(true)
        setEmptyAdapterToRecyclerView()

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner, { searchWord ->
            viewModel.getProducts().observe(viewLifecycleOwner, { products ->
                val listOfProducts = mutableListOf<ProductModel>()
                products.forEach { listOfProducts.add(it) }
                adapterFragment = ProductsFragmentRecyclerAdapter(requireActivity(), TAG,
                    listOfProducts.filter {
                        it.name.toLowerCase().contains(searchWord.toLowerCase())
                    } as ArrayList<ProductModel>, childFragmentManager, viewModelPassedToRecycler)

                recyclerView.adapter = adapterFragment
            })
        })
        return view
    }

    /**This must be called immediately in onCreate to avoid error: "E/RecyclerView: No adapterFragment attached; skipping layout" */
    private fun setEmptyAdapterToRecyclerView() {
        adapterFragment =
            ProductsFragmentRecyclerAdapter(
                requireActivity(),
                TAG, arrayListOf(), childFragmentManager, viewModelPassedToRecycler
            )
        recyclerView.adapter = adapterFragment
    }

    companion object {
        fun newInstance(): ProductsFragment = ProductsFragment()
        const val TAG = "ProductsFragment"
    }


}
