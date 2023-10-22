package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.databinding.FragmentDishesBinding
import com.erdees.foodcostcalc.domain.model.dish.GrandDishModel
import com.erdees.foodcostcalc.utils.CallbackListener
import java.util.*

// Get what to search for
// Get grand dishes
// Create a list of grand dishes with it's quantity
// Pass that list to adapter


class DishesFragment : Fragment() {
  var callbackListener: CallbackListener? = null
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

    setAdapterToRecyclerView(viewModel) { callbackListener?.callback() }
    var searchKey = ""
    var grandDishes = listOf<GrandDishModel>()
    viewModel.getWhatToSearchFor().observe(viewLifecycleOwner) {
      searchKey = it
      setAdapter(grandDishes, searchKey)
    }
    viewModel.getGrandDishes().observe(viewLifecycleOwner) {
      grandDishes = it
      setAdapter(grandDishes, searchKey)
    }
    return view
  }

  private fun setAdapter(grandDishes: List<GrandDishModel>, searchWord: String) {
    fragmentRecyclerAdapter.setGrandDishList(
      grandDishes.filter {
        it.dishModel.name.lowercase(Locale.ROOT).contains(
          searchWord.lowercase(Locale.ROOT)
        )
      })
  }

  private fun setAdapterToRecyclerView(
    viewModel: DishesFragmentViewModel,
    openCreateNewDishDialog: () -> Unit
  ) {
    fragmentRecyclerAdapter = DishesFragmentRecyclerAdapter(
      childFragmentManager,
      viewModel,
      viewLifecycleOwner,
      requireActivity(),
      openCreateNewDishDialog
    )
    binding.recyclerViewDishes.adapter = fragmentRecyclerAdapter
  }

  companion object {
    fun newInstance(): DishesFragment =
      DishesFragment()

    const val TAG = "DishesFragment"
  }
}
