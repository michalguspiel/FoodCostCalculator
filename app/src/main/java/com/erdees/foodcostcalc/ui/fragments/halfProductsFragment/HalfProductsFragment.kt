package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductWithProductsIncludedModel
import com.erdees.foodcostcalc.utils.CallbackListener
import java.util.*

class HalfProductsFragment : Fragment() {
  var callbackListener: CallbackListener? = null
  private lateinit var recyclerView: RecyclerView

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
    val adapter = HalfProductFragmentRecyclerAdapter(
      childFragmentManager,
      requireActivity()
    ) { callbackListener?.callback() }
    recyclerView.adapter = adapter

    viewModel.getWhatToSearchFor().observe(viewLifecycleOwner) { searchWord ->
      viewModel.getHalfProductWithProductIncluded()
        .observe(viewLifecycleOwner) { halfProducts ->
          val data = halfProducts.filter {
            it.halfProductModel.name.lowercase(Locale.getDefault())
              .contains(searchWord.lowercase(Locale.getDefault()))
          } as ArrayList<HalfProductWithProductsIncludedModel>
          adapter.switchList(data)
        }
    }
    return view
  }

  companion object {
    fun newInstance(): HalfProductsFragment = HalfProductsFragment()
    const val TAG = "HalfProductsFragment"
  }
}
