package com.erdees.foodcostcalc.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.fragments.dialogs.AddProductToHalfProduct
import com.erdees.foodcostcalc.fragments.dialogs.EditHalfProduct
import com.erdees.foodcostcalc.model.HalfProductWithProductsIncluded
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.HalfProductAdapterViewModel
import com.erdees.foodcostcalc.views.MaskedItemView

class HalfProductAdapter(
    private val viewLifeCycleOwner: LifecycleOwner,
    private val list: ArrayList<HalfProductWithProductsIncluded>,
    private val fragmentManager: FragmentManager,
    val viewModel: HalfProductAdapterViewModel,
    val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val LAST_ITEM_TYPE = 0
    val HALF_PRODUCT_ITEM_TYPE = 1

    inner class HalfProductsRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val eachLinearLayout: LinearLayout = view.findViewById(R.id.linear_layout_dish_card)
        private val halfProductName: TextView = view.findViewById(R.id.half_product_name_in_adapter)
        private val editButton: ImageButton = view.findViewById(R.id.edit_button_in_dish_adapter)
        private val addProductButton: ImageButton =
            view.findViewById(R.id.add_product_to_halfproduct_button)
        private val listView: ListView = view.findViewById(R.id.list_view)
        private val unitOfHalfProduct: TextView =
            view.findViewById(R.id.unit_to_populate_half_product_card_view)
        private val finalPriceOfHalfProductPerUnit: TextView =
            view.findViewById(R.id.price_of_half_product_per_unit)

        fun bind(position: Int) {
            /**Computes height of listView based on each row height, includes dividers.
             * I'm using this approach so listView size is set and doesn't need to be scrollable. */
            fun getListSize(): Int {
                var result = 0
                for (eachProduct in list[position].halfProductsList.indices) {
                    val listItem = listView.adapter.getView(eachProduct, null, listView)
                    listItem.measure(0, View.MeasureSpec.UNSPECIFIED)
                    result += listItem.measuredHeight
                }
                return result + (listView.dividerHeight * (listView.adapter.count - 1))
            }
            halfProductName.text = list[position].halfProduct.name
            unitOfHalfProduct.text = list[position].halfProduct.halfProductUnit
            viewModel
                .getCertainHalfProductWithProductsIncluded(list[position].halfProduct.halfProductId)
                .observe(viewLifeCycleOwner,
                    Observer {
                        if (it != null) finalPriceOfHalfProductPerUnit.text =
                            it.formattedPricePerUnit
                    })


            editButton.setOnClickListener {
                EditHalfProduct().show(fragmentManager, EditHalfProduct.TAG)
                EditHalfProduct.halfProductPassedFromAdapter = list[position]
            }

            addProductButton.setOnClickListener {
                AddProductToHalfProduct().show(fragmentManager, TAG)
                viewModel.passHalfProductToDialog(list[position].halfProduct)

            }

            eachLinearLayout.setOnClickListener {
                if (listView.adapter == null) {
                    listView.adapter =
                        HalfProductListViewAdapter(activity, list[position].halfProductsList)
                    listView.layoutParams =
                        LinearLayout.LayoutParams(listView.layoutParams.width, getListSize())
                } else {
                    listView.adapter = null
                    listView.layoutParams =
                        LinearLayout.LayoutParams(listView.layoutParams.width, 0)
                }
            }

        }

    }

    inner class LastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val layoutAsButton: MaskedItemView =
            view.findViewById(R.id.products_last_item_layout)
        private val text: TextView = view.findViewById(R.id.last_item_text)

        fun bind() {
            text.text = "Create Half Product"
            layoutAsButton.setOnClickListener {
                Log.i("TEST", "WORKS")
                viewModel.setOpenCreateHalfProductFlag(true)
                viewModel.setOpenCreateHalfProductFlag(false)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HALF_PRODUCT_ITEM_TYPE -> {
                val adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.half_product_card_view, parent, false)
                HalfProductsRecyclerViewHolder(adapterLayout)
            }
            else -> {
                val adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.products_recycler_last_item, parent, false)
                LastItemViewHolder(adapterLayout)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == HALF_PRODUCT_ITEM_TYPE) (holder as HalfProductAdapter.HalfProductsRecyclerViewHolder).bind(position)
        else (holder as HalfProductAdapter.LastItemViewHolder).bind()

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            list.size -> LAST_ITEM_TYPE
            else -> HALF_PRODUCT_ITEM_TYPE
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    companion object {
        const val TAG = "HalfProductAdapter"
    }

}