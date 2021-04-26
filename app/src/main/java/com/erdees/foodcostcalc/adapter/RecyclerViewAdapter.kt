package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedFunctions.formatPrice
import com.erdees.foodcostcalc.fragments.dialogs.EditProduct
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.RecyclerViewAdapterViewModel
import com.erdees.foodcostcalc.views.MaskedItemView
import java.util.*


class RecyclerViewAdapter(
    val tag: String?,
    private val list: ArrayList<Product>,
    private val fragmentManager: FragmentManager,
    val viewModel: RecyclerViewAdapterViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val amountOfItems = list.size + 1
    private val LAST_ITEM_TYPE = 0
    private val PRODUCT_ITEM_TYPE = 1


    inner class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.product_title)
        private val nettoTextView: TextView = view.findViewById(R.id.product_netto_price)
        private val foodcostTextView: TextView = view.findViewById(R.id.product_foodcost_price)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)

        fun bind(position: Int) {
            textView.text = list[position].name
            nettoTextView.text =
                "Price ${list[position].unit} netto: ${formatPrice(list[position].pricePerUnit)}."
            foodcostTextView.text =
                "Price ${list[position].unit} with foodcost: ${formatPrice(list[position].priceAfterWasteAndTax)}."
            editButton.setOnClickListener {
                EditProduct().show(fragmentManager, EditProduct.TAG)
                EditProduct.productPassedFromAdapter = list[position] as Product
            }
        }
    }

    inner class LastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val layoutAsButton: MaskedItemView =
            view.findViewById(R.id.products_last_item_layout)

        fun bind() {
            layoutAsButton.setOnClickListener {
                Log.i("TEST", "WORKS")
                viewModel.setOpenAddFlag(true)
                viewModel.setOpenAddFlag(false)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            PRODUCT_ITEM_TYPE -> {
                val adapterLayout =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_product, parent, false)
                return RecyclerViewHolder(adapterLayout)
            }
            else -> {
                val adapterLayout =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.products_recycler_last_item, parent, false)
                return LastItemViewHolder(adapterLayout)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == amountOfItems - 1) LAST_ITEM_TYPE
        else PRODUCT_ITEM_TYPE
    }

    @SuppressLint("WrongConstant", "ShowToast")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == PRODUCT_ITEM_TYPE) (holder as RecyclerViewHolder).bind(position)
        else (holder as LastItemViewHolder).bind()


    }
}


