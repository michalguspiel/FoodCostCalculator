package com.erdees.foodcostcalc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.adapter.DishAdapter
import com.erdees.foodcostcalc.model.GrandDish
import com.erdees.foodcostcalc.viewmodel.DishesViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.DishAdapterViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.DishListViewAdapterViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.HalfProductAdapterViewModel
import java.util.*

class Dishes : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var listViewViewModelPassedToRecyclerView: DishListViewAdapterViewModel
    private lateinit var viewModelPassedToRecyclerView: DishAdapterViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inflater.inflate(R.layout.fragment_dishes, container, false)
        val view: View = inflater.inflate(R.layout.fragment_dishes, container, false)

        val viewModel = ViewModelProvider(this).get(DishesViewModel::class.java)
        viewModelPassedToRecyclerView =
            ViewModelProvider(this).get(DishAdapterViewModel::class.java)
        listViewViewModelPassedToRecyclerView =
            ViewModelProvider(this).get(DishListViewAdapterViewModel::class.java)

        recyclerView = view.findViewById(R.id.recycler_view_dishes)
        recyclerView.setHasFixedSize(true)
        setEmptyAdapterToRecyclerView()

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner, { searchWord ->
            viewModel.getGrandDishes().observe(viewLifecycleOwner, { grandDishes ->
                val grandDishesList = mutableListOf<GrandDish>()
                grandDishes.forEach { grandDishesList.add(it) }
                recyclerView.adapter = DishAdapter(TAG,
                    grandDishesList.filter {
                        it.dish.name.toLowerCase().contains(searchWord.toLowerCase())
                    } as ArrayList<GrandDish>,
                    childFragmentManager, viewModelPassedToRecyclerView,
                    listViewViewModelPassedToRecyclerView,
                    viewLifecycleOwner, requireActivity())
            })
        })

        return view
    }

    /**This must be called immediately in onCreate to avoid error: "E/RecyclerView: No adapter attached; skipping layout" */
    private fun setEmptyAdapterToRecyclerView() {
        recyclerView.adapter = DishAdapter(
            TAG,
            arrayListOf(),
            childFragmentManager,
            viewModelPassedToRecyclerView,
            listViewViewModelPassedToRecyclerView,
            viewLifecycleOwner,
            requireActivity()
        )
    }

    companion object {
        fun newInstance(): Dishes = Dishes()
        const val TAG = "Dishes"
    }
}
