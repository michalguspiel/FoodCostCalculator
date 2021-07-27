package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.Constants.ADMOB_PRODUCTS_RV_AD_UNIT_ID
import com.erdees.foodcostcalc.ads.AdHelper
import com.erdees.foodcostcalc.Constants.LAST_ITEM_TYPE
import com.erdees.foodcostcalc.Constants.PRODUCTS_AD_FREQUENCY
import com.erdees.foodcostcalc.Constants.PRODUCT_AD_ITEM_TYPE
import com.erdees.foodcostcalc.Constants.PRODUCT_ITEM_TYPE
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedFunctions.formatPrice
import com.erdees.foodcostcalc.fragments.dialogs.EditProduct
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.RecyclerViewAdapterViewModel
import com.erdees.foodcostcalc.views.MaskedItemView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import java.util.*


class ProductsRecyclerAdapter(
    val activity: Activity,
    val tag: String?,
    private val list: ArrayList<Product>,
    private val fragmentManager: FragmentManager,
    val viewModel: RecyclerViewAdapterViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val adCase = AdHelper(list.size, PRODUCTS_AD_FREQUENCY)

    private val itemsSizeWithAds = adCase.finalListSize + 1 // +1 to include button as footer.

    private val positionsOfAds = adCase.positionsOfAds()

    private var currentNativeAd: NativeAd? = null

    inner class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.product_title)
        private val nettoTextView: TextView = view.findViewById(R.id.product_netto_price)
        private val foodcostTextView: TextView = view.findViewById(R.id.product_foodcost_price)
        private val editButton: ImageButton = view.findViewById(R.id.edit_button)

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {

            val positionIncludedAdsBinded = adCase.correctElementFromListToBind(position)
            Log.i("ProductsRecycler", "position : $position , positionIncludedWithAdsBinded : $positionIncludedAdsBinded , listSize : ${list.size} , positionOfAds : $positionsOfAds")
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
                viewModel.setOpenAddFlag(true)
                viewModel.setOpenAddFlag(false)
            }
        }
    }

    inner class AdItemViewHolder(val view: NativeAdView) : RecyclerView.ViewHolder(view) {
        fun bind() {
            val builder = AdLoader.Builder(activity, ADMOB_PRODUCTS_RV_AD_UNIT_ID)
            builder.forNativeAd { nativeAd ->
                // OnUnifiedNativeAdLoadedListener implementation.
                // If this callback occurs after the activity is destroyed, you must call
                // destroy and return or you may get a memory leak.
                val activityDestroyed: Boolean = activity.isDestroyed
                if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                    nativeAd.destroy()
                    Log.i("TESTTT", " THIS RETURN IS CALLED ")
                    return@forNativeAd
                }
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                currentNativeAd?.destroy()
                currentNativeAd = nativeAd
                adCase.populateNativeAdView(nativeAd, view)
            }
            val videoOptions = VideoOptions.Builder()
                .build()
            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()
            builder.withNativeAdOptions(adOptions)
            val adLoader = builder.withAdListener(object : AdListener() {
            }).build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
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
                    .inflate(R.layout.product_ad_custom_layout, parent, false) as NativeAdView
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
        return itemsSizeWithAds
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == itemsSizeWithAds - 1) LAST_ITEM_TYPE
        else if (positionsOfAds.contains(position)) PRODUCT_AD_ITEM_TYPE
        else PRODUCT_ITEM_TYPE
    }

    @SuppressLint("WrongConstant", "ShowToast")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == PRODUCT_ITEM_TYPE) (holder as ProductsRecyclerAdapter.RecyclerViewHolder).bind(
            position
        )
        else if (holder.itemViewType == PRODUCT_AD_ITEM_TYPE) (holder as ProductsRecyclerAdapter.AdItemViewHolder).bind()
        else (holder as LastItemViewHolder).bind()

    }


}
