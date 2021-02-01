package com.example.foodcostcalc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.InjectorUtils
import com.example.foodcostcalc.R
import com.example.foodcostcalc.adapter.RecyclerViewAdapter
import com.example.foodcostcalc.model.Dish
import java.util.ArrayList

class Dishes : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        inflater.inflate(R.layout.fragment_dishes,container,false)
    val view: View = inflater.inflate(R.layout.fragment_dishes, container, false)

        fun initializeUI() {
        val factory = InjectorUtils.provideAddViewModelFactory()
        val viewModel = ViewModelProviders.of(requireActivity(), factory)
                .get(AddViewModel::class.java)

            /**Implementing adapter for recycler view. */
            val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_dishes)
            recyclerView.setHasFixedSize(true)
            viewModel.getDishes().observe(this, Observer { dishes ->
                var data = mutableListOf<Dish>()
                dishes.forEach{data.add(it)}
                recyclerView.adapter =   RecyclerViewAdapter(TAG,
                        data as ArrayList<Any>,
                        childFragmentManager)
            })
    }

    initializeUI()
    return view
}

    companion object {
        fun newInstance():Dishes = Dishes()
        const val TAG = "Dishes"
    }
}
