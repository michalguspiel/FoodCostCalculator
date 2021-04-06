package com.erdees.foodcostcalc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.adapter.HalfProductAdapter
import com.erdees.foodcostcalc.model.HalfProductWithProductsIncluded
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.HalfProductAdapterViewModel
import java.util.ArrayList

class HalfProducts: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inflater.inflate(R.layout.fragment_dishes, container, false)
        val view: View = inflater.inflate(R.layout.fragment_half_products, container, false)

        val viewModel = ViewModelProvider(this).get(HalfProductsViewModel::class.java)
        val viewModelPassedToRecyclerView = ViewModelProvider(this).get(HalfProductAdapterViewModel::class.java)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_half_products)
        recyclerView.setHasFixedSize(true)

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner,  { searchWord ->
            viewModel.getHalfProductWithProductIncluded().observe(viewLifecycleOwner,  { halfProducts ->
                val listOfHalfProducts = mutableListOf<HalfProductWithProductsIncluded>()
                halfProducts.forEach { listOfHalfProducts.add(it)}
                recyclerView.adapter = HalfProductAdapter(viewLifecycleOwner,
                    listOfHalfProducts.filter{it.halfProduct.name.toLowerCase().contains(searchWord.toLowerCase())} as ArrayList<HalfProductWithProductsIncluded>,
                    childFragmentManager,viewModelPassedToRecyclerView,requireActivity())
            })
        })



        return view
    }

    companion object {
        fun newInstance(): HalfProducts = HalfProducts()
        const val TAG = "HalfProducts"
    }
}