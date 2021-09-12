package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductWithProductsIncludedModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.HalfProductAdapterViewModel
import java.util.*

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class HalfProductsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModelPassedToRecyclerView: HalfProductAdapterViewModel
    private lateinit var fragmentRecyclerAdapter: HalfProductFragmentRecyclerAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        inflater.inflate(R.layout.fragment_dishes, container, false)
        val view: View = inflater.inflate(R.layout.fragment_half_products, container, false)

        val viewModel = ViewModelProvider(this).get(HalfProductsFragmentViewModel::class.java)
        viewModelPassedToRecyclerView =
            ViewModelProvider(this).get(HalfProductAdapterViewModel::class.java)

        recyclerView = view.findViewById(R.id.recycler_view_half_products)
        recyclerView.setHasFixedSize(true)
        setEmptyAdapterToRecyclerView()

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner, { searchWord ->
            viewModel.getHalfProductWithProductIncluded()
                .observe(viewLifecycleOwner, { halfProducts ->
                    val listOfHalfProducts = mutableListOf<HalfProductWithProductsIncludedModel>()
                    halfProducts.forEach { listOfHalfProducts.add(it) }
                    fragmentRecyclerAdapter = HalfProductFragmentRecyclerAdapter(viewLifecycleOwner,
                        listOfHalfProducts.filter {
                            it.halfProductModel.name.toLowerCase()
                                .contains(searchWord.toLowerCase())
                        } as ArrayList<HalfProductWithProductsIncludedModel>,
                        childFragmentManager, viewModelPassedToRecyclerView, requireActivity())
                    recyclerView.adapter = fragmentRecyclerAdapter
                })
        })

        return view
    }

    /**This must be called immediately in onCreate to avoid error: "E/RecyclerView: No fragmentRecyclerAdapter attached; skipping layout" */
    private fun setEmptyAdapterToRecyclerView() {
        fragmentRecyclerAdapter = HalfProductFragmentRecyclerAdapter(
            viewLifecycleOwner,
            arrayListOf(),
            childFragmentManager,
            viewModelPassedToRecyclerView,
            requireActivity()
        )
        recyclerView.adapter = fragmentRecyclerAdapter
    }

    companion object {
        fun newInstance(): HalfProductsFragment = HalfProductsFragment()
        const val TAG = "HalfProductsFragment"
    }


}