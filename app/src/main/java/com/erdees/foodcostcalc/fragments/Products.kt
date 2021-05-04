package com.erdees.foodcostcalc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.adapter.RecyclerViewAdapter
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.viewmodel.ProductsViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.RecyclerViewAdapterViewModel
import java.util.ArrayList


class Products : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_products, container, false)
        val viewModel = ViewModelProvider(this).get(ProductsViewModel::class.java)
        val viewModelPassedToRecycler = ViewModelProvider(this).get(RecyclerViewAdapterViewModel::class.java)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_products)
        recyclerView.setHasFixedSize(true)

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner,{searchWord ->
            viewModel.getProducts().observe(viewLifecycleOwner,  { products ->
            val listOfProducts = mutableListOf<Product>()
            products.forEach { listOfProducts.add(it) }
            recyclerView.adapter = RecyclerViewAdapter(TAG,
                    listOfProducts.filter { it.name.toLowerCase().contains(searchWord.toLowerCase()) } as ArrayList<Product>
                    , childFragmentManager,viewModelPassedToRecycler)
        })
        })
        return view
    }
    companion object {
        fun newInstance(): Products = Products()
        const val TAG = "Products"

    }
}
