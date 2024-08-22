package com.erdees.foodcostcalc.ui.screens.halfProducts.halfProductsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.utils.CallbackListener
import kotlinx.coroutines.launch

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

    val viewModel = ViewModelProvider(this)[HalfProductsFragmentViewModel::class.java]
    recyclerView = view.findViewById(R.id.recycler_view_half_products)
    recyclerView.setHasFixedSize(true)
    setAdapterToRecyclerView(viewModel)

    lifecycleScope.launch {
      viewModel.halfProducts.collect { halfProducts ->
        adapter.setHalfProductsList(halfProducts)
      }
    }
    return view
  }

  private fun setAdapterToRecyclerView(viewModel: HalfProductsFragmentViewModel) {
    adapter = HalfProductFragmentRecyclerAdapter(
      requireActivity(),
      viewModel
    )
    recyclerView.adapter = adapter
  }

  companion object {
    fun newInstance(): HalfProductsFragment = HalfProductsFragment()
    const val TAG = "HalfProductsFragment"
  }
}
