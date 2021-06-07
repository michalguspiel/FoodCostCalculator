package com.erdees.foodcostcalc.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.adapter.ProductsRecyclerAdapter
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.viewmodel.ProductsViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.RecyclerViewAdapterViewModel
import java.util.ArrayList


class Products : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModelPassedToRecycler: RecyclerViewAdapterViewModel
    private lateinit var adapter: ProductsRecyclerAdapter



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_products, container, false)
        val viewModel = ViewModelProvider(this).get(ProductsViewModel::class.java)
        viewModelPassedToRecycler =
            ViewModelProvider(this).get(RecyclerViewAdapterViewModel::class.java)
        recyclerView = view.findViewById(R.id.recycler_view_products)
        recyclerView.setHasFixedSize(true)
        setEmptyAdapterToRecyclerView()

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner, { searchWord ->
            viewModel.getProducts().observe(viewLifecycleOwner, { products ->
                val listOfProducts = mutableListOf<Product>()
                products.forEach { listOfProducts.add(it) }
                adapter = ProductsRecyclerAdapter(requireActivity(),TAG,
                    listOfProducts.filter {
                        it.name.toLowerCase().contains(searchWord.toLowerCase())
                    } as ArrayList<Product>, childFragmentManager, viewModelPassedToRecycler)

                recyclerView.adapter = adapter
            })
        })
        return view
    }

    /**This must be called immediately in onCreate to avoid error: "E/RecyclerView: No adapter attached; skipping layout" */
    private fun setEmptyAdapterToRecyclerView() {
        adapter =
            ProductsRecyclerAdapter(requireActivity(),TAG, arrayListOf(), childFragmentManager, viewModelPassedToRecycler)
    recyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): Products = Products()
        const val TAG = "Products"
    }


}
