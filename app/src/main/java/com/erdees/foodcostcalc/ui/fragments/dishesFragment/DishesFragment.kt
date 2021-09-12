package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.GrandDishModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.DishAdapterViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.DishListViewAdapterViewModel
import java.util.*

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class DishesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var listViewViewModelPassedToRecyclerView: DishListViewAdapterViewModel
    private lateinit var viewModelPassedToRecyclerView: DishAdapterViewModel
    private lateinit var fragmentRecyclerAdapter: DishesFragmentRecyclerAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inflater.inflate(R.layout.fragment_dishes, container, false)
        val view: View = inflater.inflate(R.layout.fragment_dishes, container, false)
        val viewModel = ViewModelProvider(this).get(DishesFragmentViewModel::class.java)
        viewModelPassedToRecyclerView =
            ViewModelProvider(this).get(DishAdapterViewModel::class.java)
        listViewViewModelPassedToRecyclerView =
            ViewModelProvider(this).get(DishListViewAdapterViewModel::class.java)

        recyclerView = view.findViewById(R.id.recycler_view_dishes)
        recyclerView.setHasFixedSize(true)
        setEmptyAdapterToRecyclerView()

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner, { searchWord ->
            viewModel.getGrandDishes().observe(viewLifecycleOwner, { grandDishes ->
                val grandDishesList = mutableListOf<GrandDishModel>()
                grandDishes.forEach { grandDishesList.add(it) }
                fragmentRecyclerAdapter = DishesFragmentRecyclerAdapter(
                    TAG,
                    grandDishesList.filter {
                        it.dishModel.name.toLowerCase().contains(searchWord.toLowerCase())
                    } as ArrayList<GrandDishModel>,
                    childFragmentManager, viewModelPassedToRecyclerView,
                    listViewViewModelPassedToRecyclerView,
                    viewLifecycleOwner, requireActivity())

                recyclerView.adapter = fragmentRecyclerAdapter
            })
        })

        return view
    }

    /**This must be called immediately in onCreate to avoid error: "E/RecyclerView: No fragmentRecyclerAdapter attached; skipping layout" */
    private fun setEmptyAdapterToRecyclerView() {
        fragmentRecyclerAdapter = DishesFragmentRecyclerAdapter(
            TAG,
            arrayListOf(),
            childFragmentManager,
            viewModelPassedToRecyclerView,
            listViewViewModelPassedToRecyclerView,
            viewLifecycleOwner,
            requireActivity()
        )
        recyclerView.adapter = fragmentRecyclerAdapter
    }

    companion object {
        fun newInstance(): DishesFragment = DishesFragment()
        const val TAG = "DishesFragment"
    }


}
