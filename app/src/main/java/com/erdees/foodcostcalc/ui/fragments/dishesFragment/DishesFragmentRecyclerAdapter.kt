package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.DishCardViewBinding
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.addProductToDishDialogFragment.AddProductToDishFragment
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment.EditDishFragment
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.GrandDishModel
import com.erdees.foodcostcalc.ui.views.MaskedItemView
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Constants.DISH_AD_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.DISH_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.LAST_ITEM_TYPE
import com.erdees.foodcostcalc.utils.ViewUtils.getListSize
import com.erdees.foodcostcalc.utils.ads.AdHelper
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.play.core.review.ReviewManagerFactory
import io.reactivex.subjects.PublishSubject
import java.util.*

class DishesFragmentRecyclerAdapter(
    private val fragmentManager: FragmentManager,
    val viewModel: DishRVAdapterViewModel,
    private val dishListViewAdapterViewModel: DishListViewAdapterViewModel,
    val viewLifecycleOwner: LifecycleOwner,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var list: List<GrandDishModel>
    lateinit var adCase: AdHelper
    private var currentNativeAd: NativeAd? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setGrandDishList(listToSet: List<GrandDishModel>) {
        this.list = listToSet
        this.notifyDataSetChanged()
    }

    fun initializeAdHelper() {
        adCase = AdHelper(list.size, Constants.DISHES_AD_FREQUENCY)
    }

    inner class DishRecyclerViewHolder(private val viewBinding: DishCardViewBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {


        private var amountOfServingsToPresent = 1
        private val amountOfServingsSubject = PublishSubject.create<Int>()

        private fun setPriceData(amountOfServings: Int, dishModelId: Long) {
            viewBinding.totalPriceDishCardView.text =
                viewModel.formattedPriceData(dishModelId, amountOfServings)
            viewBinding.totalPriceWithMarginDishCardView.text = viewModel.formattedTotalPriceData(
                dishModelId,
                amountOfServings
            )
        }

        private fun setHowManyServingsTV(i: Int) {
            if (i == 1) viewBinding.howManyServingsTextView.text =
                activity.getString(R.string.data_per_serving)
            else viewBinding.howManyServingsTextView.text =
                activity.getString(R.string.data_per_x_servings, i)
        }

        /**Summing up total price of products included and then one by one adding price of each half product.*/
        private fun sumPriceAndSetPriceData(position: Int) {
            list[position].halfProducts.forEach {
                viewModel
                    .getCertainHalfProductWithProductsIncluded(it.halfProductOwnerId)
                    .observe(viewLifecycleOwner, { halfProductWithProductsIncluded ->
                        viewModel.addToTotalPrice(
                            list[position].dishModel.dishId,
                            halfProductWithProductsIncluded.pricePerUnit(),
                            it.weight,
                            halfProductWithProductsIncluded.halfProductModel.halfProductUnit,
                            it.unit
                        )
                        if (isThisLastItemOfTheList(
                                list[position].halfProducts.indexOf(it),
                                list[position].halfProducts.size
                            )
                        ) setPriceData(amountOfServingsToPresent, list[position].dishModel.dishId)
                    })
            }
        }

        private fun setNameTaxAndMarginAccordingly(position: Int) {
            viewBinding.dishNameInAdapter.text = list[position].dishModel.name
            viewBinding.dishMarginTvInAdapter.text = (activity.getString(
                R.string.dish_x_margin, String.format(
                    list[position].dishModel.marginPercent.toString()
                )
            ))
            viewBinding.dishTaxTvInAdapter.text = activity.getString(
                R.string.dish_x_tax, String.format(
                    list[position].dishModel.dishTax.toString()
                )
            )
        }

        private fun setButtons(position: Int) {
            viewBinding.editButtonInDishAdapter.setOnClickListener {
                openDialog(EditDishFragment())
                EditDishFragment.grandDishModelPassedFromAdapter = list[position]
            }
            viewBinding.addProductToDishButton.setOnClickListener {
                AddProductToDishFragment.dishModelPassedFromAdapter = list[position].dishModel
                openDialog(AddProductToDishFragment())
            }
        }


        private fun setAdapterToTheList(position: Int) {
            viewBinding.listView.adapter =
                makeAdapterForList(position, amountOfServingsToPresent)
            val indicesOfBothLists =
                list[position].productsIncluded.indices + list[position].halfProducts.indices
            viewBinding.listView.layoutParams =
                LinearLayout.LayoutParams(
                    viewBinding.listView.layoutParams.width,
                    getListSize(indicesOfBothLists, viewBinding.listView)
                )
        }

        private fun makeDishCardOpenable(position: Int) {
            viewBinding.linearLayoutDishCard.setOnClickListener {
                if (viewBinding.listView.adapter == null) {
                    viewBinding.howManyServingsTextView.visibility = View.VISIBLE
                    setAdapterToTheList(position)
                } else {
                    viewBinding.howManyServingsTextView.visibility = View.GONE
                    viewBinding.listView.adapter = null
                    viewBinding.listView.layoutParams =
                        LinearLayout.LayoutParams(viewBinding.listView.layoutParams.width, 0)
                }
            }
        }

        private fun setPositiveButtonFunctionality(
            positiveButton: Button,
            editText: EditText,
            alertDialog: AlertDialog,
            position: Int
        ) {
            positiveButton.setOnClickListener {
                if (editText.text.isNullOrBlank() || !editText.text.isDigitsOnly()) {
                    Toast.makeText(activity, "Wrong input!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                amountOfServingsSubject.onNext(editText.text.toString().toInt())
                viewBinding.listView.adapter =
                    makeAdapterForList(position, amountOfServingsToPresent) // TO REFRESH LIST
                val indicesOfBothLists =
                    list[position].productsIncluded.indices + list[position].halfProducts.indices
                viewBinding.listView.layoutParams =
                    LinearLayout.LayoutParams(
                        viewBinding.listView.layoutParams.width,
                        getListSize(indicesOfBothLists, viewBinding.listView)
                    ) // TO CHANGE SIZE TOO
                alertDialog.dismiss()
            }
        }

        private fun setHowManyServingsTvAsButton(textView: TextView, position: Int) {
            textView.setOnClickListener {
                val textInputLayout =
                    activity.layoutInflater.inflate(R.layout.text_input_layout, null)
                val editText =
                    textInputLayout.findViewById<EditText>(R.id.text_input_layout_quantity)
                val linearLayout = LinearLayout(activity)
                val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(25, 0, 25, 0)
                editText.setText(amountOfServingsToPresent.toString())
                linearLayout.addView(textInputLayout, params)
                val alertDialog = AlertDialog.Builder(activity)
                    .setMessage(R.string.serving_amount)
                    .setView(linearLayout)
                    .setPositiveButton(R.string.submit, null)
                    .setNegativeButton(R.string.back, null)
                    .show()
                alertDialog.window?.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.background_for_dialogs
                    )
                )
                val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                setPositiveButtonFunctionality(positiveButton, editText, alertDialog, position)
            }
        }

        private fun setDishData(position: Int) {
            viewModel.setDishData(
                list[position].dishModel.dishId,
                list[position].totalPrice,
                list[position].dishModel.marginPercent,
                list[position].dishModel.dishTax
            )
        }

        @SuppressLint("CheckResult")
        fun bind(position: Int) {
            val positionIncludedAdsBinded = adCase.correctElementFromListToBind(position)
            if (position == 3) openFeedBackForm()
            setDishData(positionIncludedAdsBinded)
            amountOfServingsSubject.subscribe { i ->
                amountOfServingsToPresent = i
                setHowManyServingsTV(i)
                setPriceData(amountOfServingsToPresent, list[position].dishModel.dishId)
            }
            amountOfServingsSubject.onNext(1)
            sumPriceAndSetPriceData(positionIncludedAdsBinded)
            setNameTaxAndMarginAccordingly(positionIncludedAdsBinded)
            setButtons(positionIncludedAdsBinded)
            makeDishCardOpenable(positionIncludedAdsBinded)
            setHowManyServingsTvAsButton(
                viewBinding.howManyServingsTextView,
                positionIncludedAdsBinded
            )
            viewModel.getGrandDishes.observe(viewLifecycleOwner, {
                if (viewBinding.listView.adapter == null) {
                } else setAdapterToTheList(position)
            })
        }
    }

    inner class AdItemViewHolder(val view: NativeAdView) : RecyclerView.ViewHolder(view) {
        fun bind() {
            val builder = AdLoader.Builder(activity, Constants.ADMOB_DISHES_RV_AD_UNIT_ID)
            builder.forNativeAd { nativeAd ->
                // OnUnifiedNativeAdLoadedListener implementation.
                // If this callback occurs after the activity is destroyed, you must call
                // destroy and return or you may get a memory leak.
                val activityDestroyed: Boolean = activity.isDestroyed
                if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                    nativeAd.destroy()
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

    inner class LastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val layoutAsButton: MaskedItemView =
            view.findViewById(R.id.products_last_item_layout)
        private val text: TextView = view.findViewById(R.id.last_item_text)

        fun bind() {
            text.text = activity.getString(R.string.create_dish)
            layoutAsButton.setOnClickListener {
                /**TODO IN THE FUTURE CHANGE FUNCTIONALITY OF THIS*/
                viewModel.setOpenCreateDishFlag(true)
                viewModel.setOpenCreateDishFlag(false)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DISH_ITEM_TYPE -> {
                DishRecyclerViewHolder(
                    DishCardViewBinding.inflate(
                        LayoutInflater.from(activity),
                        parent,
                        false
                    )
                )
            }
            DISH_AD_ITEM_TYPE -> {
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
        return adCase.itemsSizeWithAds()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == adCase.itemsSizeWithAds() - 1) LAST_ITEM_TYPE
        else if (adCase.positionsOfAds().contains(position)) DISH_AD_ITEM_TYPE
        else DISH_ITEM_TYPE
    }

    @SuppressLint("WrongConstant", "ShowToast", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == DISH_ITEM_TYPE) (holder as DishRecyclerViewHolder).bind(
            position
        )
        else if (holder.itemViewType == DISH_AD_ITEM_TYPE) (holder as AdItemViewHolder).bind()
        else (holder as LastItemViewHolder).bind()
    }

    private fun openDialog(dialog: DialogFragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.addToBackStack(dialog.tag)
        dialog.show(transaction, TAG)
    }

    private fun makeAdapterForList(position: Int, servings: Int): ListAdapter {
        return DishesDetailedListViewAdapter(
            activity,
            list[position],
            servings,
            dishListViewAdapterViewModel,
            viewLifecycleOwner
        )
    }

    private fun openFeedBackForm() {
        Log.i("Main Activity", "review triggered!")
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { thisRequest ->
            if (thisRequest.isSuccessful) {
                val reviewInfo = thisRequest.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    Log.i("Main Activity", "review success!")
                }
            } else {
                Log.i("Main Activity", "review fail!")
            }
        }
    }

    companion object {
        const val TAG = "DishesFragmentRecyclerAdapter"
    }

    private fun isThisLastItemOfTheList(indexOfHalfProduct: Int, listSize: Int): Boolean {
        return (indexOfHalfProduct == listSize - 1)
    }
}