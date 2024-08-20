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
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import kotlinx.coroutines.launch

class DishesFragment : Fragment() {

  var navigateToAddItemToDishScreen: ((Long, String) -> Unit) = { _, _ -> }
  var navigateToEditDishScreen: (DishDomain) -> Unit = { _ -> }

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

    setAdapterToRecyclerView(
      viewModel,
      navigateToAddItemToDishScreen = { id, name ->
        navigateToAddItemToDishScreen(id, name)
      },
      navigateToEditDishScreen = { dish ->
        navigateToEditDishScreen(dish)
      }
    )

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
    navigateToAddItemToDishScreen: ((Long, String) -> Unit),
    navigateToEditDishScreen: (DishDomain) -> Unit = { _ -> }
  ) {
    fragmentRecyclerAdapter = DishesFragmentRecyclerAdapter(
      viewModel = viewModel,
      activity = requireActivity(),
      navigateToAddItemsToDish = { id, name -> navigateToAddItemToDishScreen(id, name) },
      navigateToEditDish = { dish -> navigateToEditDishScreen(dish) })
    binding.recyclerViewDishes.adapter = fragmentRecyclerAdapter
  }

  companion object {
    fun newInstance(): DishesFragment =
      DishesFragment()

    const val TAG = "DishesFragment"
  }
}
