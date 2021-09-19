package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.databinding.FragmentDishesBinding
import java.util.*

class DishesFragment : Fragment() {

    private lateinit var listViewViewModelPassedToRecyclerView: DishListViewAdapterViewModel
    private lateinit var viewModelPassedToRecyclerView: DishRVAdapterViewModel
    private lateinit var fragmentRecyclerAdapter: DishesFragmentRecyclerAdapter

    private var _binding: FragmentDishesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishesBinding.inflate(inflater, container, false)
        val view = binding.root
        val viewModel = ViewModelProvider(this).get(DishesFragmentViewModel::class.java)
        viewModelPassedToRecyclerView =
            ViewModelProvider(this).get(DishRVAdapterViewModel::class.java)
        listViewViewModelPassedToRecyclerView =
            ViewModelProvider(this).get(DishListViewAdapterViewModel::class.java)

        binding.recyclerViewDishes.setHasFixedSize(true)
        setAdapterToRecyclerView()

        viewModel.getWhatToSearchFor().observe(viewLifecycleOwner, { searchWord ->
            viewModel.getGrandDishes().observe(viewLifecycleOwner, { grandDishes ->
                fragmentRecyclerAdapter.setGrandDishList(
                    grandDishes.filter {
                        it.dishModel.name.toLowerCase(Locale.ROOT).contains(
                            searchWord.toLowerCase(
                                Locale.ROOT
                            )
                        )
                    })
                fragmentRecyclerAdapter.initializeAdHelper()
            })
        })
        return view
    }

    private fun setAdapterToRecyclerView() {
        fragmentRecyclerAdapter = DishesFragmentRecyclerAdapter(
            TAG,
            childFragmentManager,
            viewModelPassedToRecyclerView,
            listViewViewModelPassedToRecyclerView,
            viewLifecycleOwner,
            requireActivity()
        )
        fragmentRecyclerAdapter.setGrandDishList(arrayListOf())
        fragmentRecyclerAdapter.initializeAdHelper()
        binding.recyclerViewDishes.adapter = fragmentRecyclerAdapter
    }

    companion object {
        fun newInstance(): DishesFragment = DishesFragment()
        const val TAG = "DishesFragment"
    }
}