package com.erdees.foodcostcalc.ui.screens.dishes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.erdees.foodcostcalc.databinding.FragmentDishesBinding
import kotlinx.coroutines.launch

class DishesFragment : Fragment() {

  var navigateToAddItemToDishScreen: ((Long, String) -> Unit) = { _, _ -> }

  private lateinit var fragmentRecyclerAdapter: DishesFragmentRecyclerAdapter

  private var _binding: FragmentDishesBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    Log.i(TAG, "onCreateView")
    _binding = FragmentDishesBinding.inflate(inflater, container, false)
    val view = binding.root
    val viewModel = ViewModelProvider(this)[DishesFragmentViewModel::class.java]

    setAdapterToRecyclerView(viewModel) { id, name ->
      navigateToAddItemToDishScreen(id, name)
    }

    lifecycleScope.launch {
      viewModel.dishes.collect {
        Log.i(TAG, "Collected dishes $it")
        fragmentRecyclerAdapter.setDishList(it)
      }
    }
    return view
  }

  private fun setAdapterToRecyclerView(
    viewModel: DishesFragmentViewModel,
    navigateToAddItemToDishScreen: ((Long, String) -> Unit)
  ) {
    fragmentRecyclerAdapter = DishesFragmentRecyclerAdapter(
      viewModel,
      requireActivity()
    ) { id, name -> navigateToAddItemToDishScreen(id, name) }
    binding.recyclerViewDishes.adapter = fragmentRecyclerAdapter
  }

  companion object {
    fun newInstance(): DishesFragment =
      DishesFragment()

    const val TAG = "DishesFragment"
  }
}
