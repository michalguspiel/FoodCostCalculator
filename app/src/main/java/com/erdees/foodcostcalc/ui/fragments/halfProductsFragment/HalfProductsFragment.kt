package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.activities.mainActivity.MainActivity
import com.erdees.foodcostcalc.utils.CallbackListener
import java.util.*

// todo fix
class HalfProductsFragment : Fragment() {
  var callbackListener: CallbackListener? = null
  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: HalfProductFragmentRecyclerAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    inflater.inflate(R.layout.fragment_dishes, container, false)
    val view: View = inflater.inflate(R.layout.fragment_half_products, container, false)

    val viewModel = ViewModelProvider(this).get(HalfProductsFragmentViewModel::class.java)
    recyclerView = view.findViewById(R.id.recycler_view_half_products)
    recyclerView.setHasFixedSize(true)
    setAdapterToRecyclerView(viewModel)

    var searchKeyWord = ""
//    var halfProducts = listOf<HalfProductWithProductsIncluded>()
    viewModel.getWhatToSearchFor().observe(viewLifecycleOwner) { searchWord ->
      searchKeyWord = searchWord
//      setAdapter(halfProducts, searchKeyWord)
    }
    return view
  }

//  private fun setAdapter(halfProducts: List<HalfProductWithProductsIncluded>, searchWord: String) {
//    val data = halfProducts.filter {
//      it.halfProduct.name.lowercase(Locale.getDefault())
//        .contains(searchWord.lowercase(Locale.getDefault()))
//    } as ArrayList<HalfProductWithProductsIncluded>
//    adapter.setHalfProductsList(data)
//  }

  private fun setAdapterToRecyclerView(viewModel: HalfProductsFragmentViewModel) {
    adapter = HalfProductFragmentRecyclerAdapter(
      childFragmentManager,
      requireActivity(),
      viewModel
    ) { callbackListener?.callback() }
    recyclerView.adapter = adapter
  }

  override fun onResume() {
    (activity as MainActivity).setSearchToolbar()
    super.onResume()
  }

  companion object {
    fun newInstance(): HalfProductsFragment = HalfProductsFragment()
    const val TAG = "HalfProductsFragment"
  }
}
