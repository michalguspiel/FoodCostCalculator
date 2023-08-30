package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.databinding.FragmentDishesBinding
import com.erdees.foodcostcalc.utils.CallbackListener
import java.util.*

class DishesFragment : Fragment() {
  var callbackListener: CallbackListener? = null
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

    setAdapterToRecyclerView{ callbackListener?.callback() }

    viewModel.getWhatToSearchFor().observe(viewLifecycleOwner) { searchWord ->
      viewModel.getGrandDishes().observe(viewLifecycleOwner) { grandDishes ->
        fragmentRecyclerAdapter.setGrandDishList(
          grandDishes.filter {
            it.dishModel.name.lowercase(Locale.ROOT).contains(
              searchWord.lowercase(Locale.ROOT)
            )
          })
      }
    }
    return view
  }

  private fun setAdapterToRecyclerView(openCreateNewDishDialog: () -> Unit) {
    fragmentRecyclerAdapter = DishesFragmentRecyclerAdapter(
      childFragmentManager,
      viewModelPassedToRecyclerView,
      viewLifecycleOwner,
      requireActivity(),
      openCreateNewDishDialog
    )
    binding.recyclerViewDishes.adapter = fragmentRecyclerAdapter
  }

  override fun onDestroy() {
    fragmentRecyclerAdapter.onDestroy()
    super.onDestroy()
  }

  companion object {
    fun newInstance(): DishesFragment =
      DishesFragment()

    const val TAG = "DishesFragment"
  }
}
