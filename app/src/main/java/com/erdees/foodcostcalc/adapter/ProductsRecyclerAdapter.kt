package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.erdees.AdHelper
import com.erdees.Constants
import com.erdees.Constants.LAST_ITEM_TYPE
import com.erdees.Constants.PRODUCTS_AD_FREQUENCY
import com.erdees.Constants.PRODUCT_AD_ITEM_TYPE
import com.erdees.Constants.PRODUCT_ITEM_TYPE
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedFunctions.formatPrice
import com.erdees.foodcostcalc.fragments.dialogs.EditProduct
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.RecyclerViewAdapterViewModel
import com.erdees.foodcostcalc.views.MaskedItemView
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.play.core.internal.ad
import java.util.*


class ProductsRecyclerAdapter(
    val tag: String?,
    private val list: ArrayList<Product>,
    private val fragmentManager: FragmentManager,
    val viewModel: RecyclerViewAdapterViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val adCase = AdHelper(list.size, PRODUCTS_AD_FREQUENCY)

    private val itemsSizeWithAds = adCase.newListSizeWithAds + 1 // +1 to include button as footer.

    private val positionsOfAds = adCase.positionsOfAds()

    inner class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.product_title)
        private val nettoTextView: TextView = view.findViewById(R.id.product_netto_price)
        private val foodcostTextView: TextView = view.findViewById(R.id.product_foodcost_price)
        private val editButton: ImageButton = view.findViewById(R.id.edit_button)

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {

            val positionIncludedAdsBinded = adCase.correctElementFromListToBind(position)

            Log.i("binding product", positionIncludedAdsBinded.toString())
            Log.i("bindinglistsizewithads", itemsSizeWithAds.toString())
            Log.i("binding  listsize", list.size.toString())
            positionsOfAds.forEach { Log.i("binding product", it.toString()) }

            textView.text = list[positionIncludedAdsBinded].name
            nettoTextView.text =
                "Price ${list[positionIncludedAdsBinded].unit} netto: ${formatPrice(list[positionIncludedAdsBinded].pricePerUnit)}."
            foodcostTextView.text =
                "Price ${list[positionIncludedAdsBinded].unit} with foodcost: ${formatPrice(list[positionIncludedAdsBinded].priceAfterWasteAndTax)}."
            editButton.setOnClickListener {
                EditProduct().show(fragmentManager, EditProduct.TAG)
                EditProduct.productPassedFromAdapter = list[positionIncludedAdsBinded]
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

    inner class AdItemViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val headline  = view.findViewById<TextView>(R.id.ad_headline)
        private val icon = view.findViewById<ImageView>(R.id.ad_app_icon)

        fun bind(){}

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PRODUCT_ITEM_TYPE -> {
                val adapterLayout =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_product, parent, false)
                RecyclerViewHolder(adapterLayout)
            }
            PRODUCT_AD_ITEM_TYPE -> {
                val adapterLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.product_ad_layout,parent,false) as NativeAdView
                AdItemViewHolder(adapterLayout)
            }
            else -> {
                val adapterLayout =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.products_recycler_last_item, parent, false)
                LastItemViewHolder(adapterLayout)
            }
        }
    }

    override fun getItemCount(): Int {
        Log.i("TEST", itemsSizeWithAds.toString())
        return itemsSizeWithAds
    }


    override fun getItemViewType(position: Int): Int {
        return if ( position == itemsSizeWithAds - 1) LAST_ITEM_TYPE
        else if (positionsOfAds.contains(position)) PRODUCT_AD_ITEM_TYPE
        else PRODUCT_ITEM_TYPE
        }

    @SuppressLint("WrongConstant", "ShowToast")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == PRODUCT_ITEM_TYPE) (holder as RecyclerViewHolder).bind(position)
        else if(holder.itemViewType == PRODUCT_AD_ITEM_TYPE) (holder as AdItemViewHolder).bind()
        else (holder as LastItemViewHolder).bind()


    }
}


