package com.example.foodcostcalc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.R
import com.example.foodcostcalc.adapter.RecyclerViewAdapter
import com.example.foodcostcalc.model.Product
import java.util.ArrayList


class Products : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_products, container, false)
        /** initialize ui with viewmodel*/
        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)



            /**Implementing adapter for recycler view. */
            val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_products)
            recyclerView.setHasFixedSize(true)
            viewModel.readAllProductData.observe(viewLifecycleOwner, Observer { products ->
                var data = mutableListOf<Product>()
                products.forEach{data.add(it) }
                recyclerView.adapter = RecyclerViewAdapter(TAG, data as ArrayList<*>, childFragmentManager)

            })







        return view
    }
    companion object {
        fun newInstance():Products = Products()
        const val TAG = "Products"

    }
}
