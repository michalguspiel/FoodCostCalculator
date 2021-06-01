package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.AdHelper
import com.erdees.foodcostcalc.Constants
import com.erdees.foodcostcalc.Constants.LAST_ITEM_TYPE
import com.erdees.foodcostcalc.Constants.PRODUCTS_AD_FREQUENCY
import com.erdees.foodcostcalc.Constants.PRODUCT_AD_ITEM_TYPE
import com.erdees.foodcostcalc.Constants.PRODUCT_ITEM_TYPE
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedFunctions.formatPrice
import com.erdees.foodcostcalc.ads.NativeTemplateStyle
import com.erdees.foodcostcalc.ads.TemplateView
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
    val context: Context,
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

    inner class AdItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            val adLoader:AdLoader = AdLoader.Builder(context, Constants.ADMOB_TEST_AD_UNIT_ID)
                .forNativeAd { nativeAd ->
                    Log.i("TESt", "Ad downloaded succesfully ${nativeAd.body} , ${nativeAd.headline}")
                    val styles: NativeTemplateStyle =
                        NativeTemplateStyle.Builder().withMainBackgroundColor(
                            ColorDrawable(
                                ContextCompat.getColor(
                                    context,
                                    R.color.gray_200
                                )
                            )
                        ).build();
                    val template: TemplateView = view.findViewById(R.id.my_template);
                    template.setStyles(styles);
                    template.setNativeAd(nativeAd);
                }
                .build();
            adLoader.loadAd(AdRequest.Builder().build());
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
                    .inflate(R.layout.product_ad_layout, parent, false)
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
        if (holder.itemViewType == PRODUCT_ITEM_TYPE) (holder as ProductsRecyclerAdapter.RecyclerViewHolder).bind(position)
        else if (holder.itemViewType == PRODUCT_AD_ITEM_TYPE) (holder as ProductsRecyclerAdapter.AdItemViewHolder).bind()
        else (holder as LastItemViewHolder).bind()

    }


}
