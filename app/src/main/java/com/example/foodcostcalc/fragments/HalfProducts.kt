package com.example.foodcostcalc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.R
import com.example.foodcostcalc.adapter.DishAdapter
import com.example.foodcostcalc.adapter.HalfProductAdapter
import com.example.foodcostcalc.adapter.RecyclerViewAdapter
import com.example.foodcostcalc.model.DishWithProductsIncluded
import com.example.foodcostcalc.model.HalfProduct
import com.example.foodcostcalc.model.HalfProductWithProductsIncluded
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.example.foodcostcalc.viewmodel.HalfProductsViewModel
import java.util.ArrayList

class HalfProducts: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inflater.inflate(R.layout.fragment_dishes, container, false)
        val view: View = inflater.inflate(R.layout.fragment_half_products, container, false)
        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)
        val halfProductViewModel = ViewModelProvider(this).get(HalfProductsViewModel::class.java)

        /**Implementing adapter for recycler view. */
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_half_products)
        recyclerView.setHasFixedSize(true)

        /** Observe what is in the search box in LIVEDATA */
        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner, Observer { word ->
            halfProductViewModel.getHalfProductWithProductIncluded().observe(viewLifecycleOwner, Observer { dish ->
                val data = mutableListOf<HalfProductWithProductsIncluded>()
                dish.forEach { data.add(it)}
                recyclerView.adapter = HalfProductAdapter(viewLifecycleOwner,
                    data.filter{it.halfProduct.name.toLowerCase().contains(word.toLowerCase())} as ArrayList<HalfProductWithProductsIncluded>,
                    childFragmentManager,viewModel,halfProductViewModel,requireActivity())
            })
        })



        return view
    }

    companion object {
        fun newInstance(): HalfProducts = HalfProducts()
        const val TAG = "HalfProducts"
    }
}