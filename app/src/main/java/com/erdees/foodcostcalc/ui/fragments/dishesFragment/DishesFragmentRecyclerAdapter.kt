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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.DishCardViewBinding
import com.erdees.foodcostcalc.databinding.ListviewDishRowBinding
import com.erdees.foodcostcalc.domain.model.dish.GrandDishModel
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.domain.model.product.ProductIncluded
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.addProductToDishDialogFragment.AddProductToDishFragment
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment.EditDishFragment
import com.erdees.foodcostcalc.ui.views.MaskedItemView
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Constants.DISH_AD_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.DISH_ITEM_TYPE
import com.erdees.foodcostcalc.utils.Constants.LAST_ITEM_TYPE
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.ViewUtils.makeGone
import com.erdees.foodcostcalc.utils.ViewUtils.makeVisible
import com.erdees.foodcostcalc.utils.ads.AdHelper
import com.erdees.foodcostcalc.utils.diffutils.GrandDishDiffUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.play.core.review.ReviewManagerFactory
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.text.NumberFormat

// TODO : Fix it, buggy AF
class DishesFragmentRecyclerAdapter(
  private val fragmentManager: FragmentManager,
  private val viewModel: DishRVAdapterViewModel,
  private val viewLifecycleOwner: LifecycleOwner,
  private val activity: Activity,
  private val openCreateNewDishDialog: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  var list: List<GrandDishModel> = listOf()
  var adCase: AdHelper = AdHelper(list.size, Constants.DISHES_AD_FREQUENCY)
  private var currentNativeAd: NativeAd? = null
  private var positionOfListAdapterToUpdate: Int? = null

  fun setGrandDishList(listToSet: List<GrandDishModel>) {
    Log.i(TAG,"setGrandDishList")
    val diffUtil = GrandDishDiffUtil(oldList = this.list, newList = listToSet)
    val diffResult = DiffUtil.calculateDiff(diffUtil)
    this.list = listToSet
    diffResult.dispatchUpdatesTo(this)
    refreshAdHelper()
    cleanupSubjects(list)
    notifyDataSetChanged()
  }

  private fun refreshAdHelper() {
    adCase = AdHelper(list.size, Constants.DISHES_AD_FREQUENCY)
  }

  private fun cleanupSubjects(list: List<GrandDishModel>) {
    val subjects = amountOfServingsSubjects.filter { subject ->
      list.map { it.dishModel.dishId }.contains(subject.first)
    }
    amountOfServingsSubjects = subjects
    val disposables = disposables.filterNot { disposable ->
      list.map { it.dishModel.dishId }.contains(disposable.first)
    }
    disposables.forEach { it.second.dispose() }
    val newCurrentValueMap = currentValueMap.filter { value ->
      list.map { it.dishModel.dishId }.contains(value.key)
    }
    currentValueMap = newCurrentValueMap.toMutableMap()
  }

  private var amountOfServingsSubjects: List<Pair<Long, PublishSubject<Int>>> = listOf()
  private var disposables: List<Pair<Long, Disposable>> = listOf()
  private var currentValueMap: MutableMap<Long, Int> = mutableMapOf()

  fun getPublishSubjectById(itemId: Long): PublishSubject<Int>? {
    return amountOfServingsSubjects.find { it.first == itemId }?.second
  }

  fun onDestroy() {
    disposables.forEach { (dishId, disposable) ->
      disposable.dispose()
    }
  }

  inner class DishRecyclerViewHolder(private val viewBinding: DishCardViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {

    private fun setPriceData(amountOfServings: Int, dishModelId: Long) {
      Log.i(TAG, "Setting price data for dish $dishModelId with: $amountOfServings servings")
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
        activity.getString(R.string.data_per_x_servings, i.toString())
    }

    /**Summing up total price of products included and then one by one adding price of each half product.*/
    private fun sumPriceAndSetPriceData(position: Int) {
      Log.i(TAG, "sumPriceAndSetPriceData, $position")
      if (list[position].halfProducts.isEmpty()) setPriceData(
        getAmountOfServings(position),
        list[position].dishModel.dishId
      )

      list[position].halfProducts.forEach {
        viewModel
          .getCertainHalfProductWithProductsIncluded(it.halfProductOwnerId)
          .observe(viewLifecycleOwner) { halfProductWithProductsIncluded ->
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
            ) {
              setPriceData(getAmountOfServings(position), list[position].dishModel.dishId)
            }
          }
      }
    }

    private fun getAmountOfServings(position: Int): Int {
      return currentValueMap[list[position].dishModel.dishId] ?: 1
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
        positionOfListAdapterToUpdate = position
        openDialog(EditDishFragment())
        EditDishFragment.grandDishModelPassedFromAdapter = list[position]
      }
      viewBinding.addProductToDishButton.setOnClickListener {
        Log.i(TAG,"opening dialog of $position ${list[position].dishModel.name} ")
        positionOfListAdapterToUpdate = position
        AddProductToDishFragment.dishModelPassedFromAdapter = list[position].dishModel
        openDialog(AddProductToDishFragment())
      }
    }


    private fun setIngredientList(position: Int) {
      if(viewBinding.ingredientList.isGone) return
      Log.i(TAG,"setIngredientList")
      viewBinding.ingredientList.removeAllViews()
      val dish = list[position]
      val servings = getAmountOfServings(position)
      val ingredients = (dish.productsIncluded + dish.halfProducts)
      ingredients.forEachIndexed { index, ingredient ->
        val row =  ListviewDishRowBinding.inflate(LayoutInflater.from(activity))
        when (ingredient) {
          is HalfProductIncludedInDishModel -> {
            setRowAsHalfProduct(row,ingredient,servings)
          }
          is ProductIncluded -> setRowAsProduct(ingredient,row,servings)
        }
        viewBinding.ingredientList.addView(
         row.root
        )
        if (index < ingredients.size - 1) {
          Log.i(TAG,"Index : $index, size : ${ingredients.size} , adding divider")
          val divider = View(activity)
          divider.setBackgroundColor(ContextCompat.getColor(activity, R.color.gray_200))
          divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
          )
          viewBinding.ingredientList.addView(
            divider
          )
        }
      }
    }
    private fun setRowAsProduct(product: ProductIncluded, view: ListviewDishRowBinding,servings: Int) {
      view.productNameInDishRow.text =
        product.productModelIncluded.name
      view.productWeightInDishRow.text =
        Utils.formatPriceOrWeight(product.weight * servings)
      view.productPriceInDishRow.text = NumberFormat.getCurrencyInstance()
        .format(product.totalPriceOfThisProduct * servings)
      view.productWeightUnitInDishRow.text =
        UnitsUtils.getUnitAbbreviation(product.weightUnit)
    }

    private fun setHalfProductRowPrice(servings: Int,halfProduct: HalfProductIncludedInDishModel, productPriceTextView: TextView) {
      viewModel.getCertainHalfProductWithProductsIncluded(halfProduct.halfProductOwnerId)
        .observe(
          viewLifecycleOwner
        ) {
          productPriceTextView.text = Utils.formatPrice(
            UnitsUtils.calculatePrice(
              it.pricePerUnit(),
              halfProduct.weight,
              it.halfProductModel.halfProductUnit,
              halfProduct.unit
            ) * servings
          )
        }
    }

    private fun setRowAsHalfProduct(
      view: ListviewDishRowBinding,
      halfProduct: HalfProductIncludedInDishModel,
      servings: Int
    ) {
      view.productNameInDishRow.text =
        halfProduct.halfProductModel.name
      view.productWeightInDishRow.text =
        (halfProduct.weight * servings).toString()
      view.productWeightUnitInDishRow.text =
        UnitsUtils.getUnitAbbreviation(halfProduct.unit)
      setHalfProductRowPrice(servings,halfProduct,view.productPriceInDishRow)
    }

    private fun makeDishCardOpenable(position: Int) {
      viewBinding.linearLayoutDishCard.setOnClickListener {
        if (viewBinding.ingredientList.isVisible) {
          viewBinding.ingredientList.makeGone()
          viewBinding.howManyServingsTextView.makeGone()
        } else {
          viewBinding.ingredientList.makeVisible()
          viewBinding.howManyServingsTextView.makeVisible()
          setIngredientList(position)
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
        getPublishSubjectById(list[position].dishModel.dishId)?.onNext(
          editText.text.toString().toInt()
        )
        alertDialog.dismiss()
      }
    }

    private fun setHowManyServingsTvAsButton(textView: TextView, position: Int) {
      textView.setOnClickListener {
        positionOfListAdapterToUpdate = position
        val textInputLayout =
          activity.layoutInflater.inflate(R.layout.text_input_layout, null)
        val editText =
          textInputLayout.findViewById<EditText>(R.id.text_input_layout_quantity)
        val linearLayout = LinearLayout(activity)
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(25, 0, 25, 0)
        editText.setText(getAmountOfServings(position).toString())
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

    private fun createSubjectIfDoesNotExist(position: Int) {
      val thisDishId = list[position].dishModel.dishId
      val subject = getPublishSubjectById(thisDishId)
      if (subject == null) {
        val newSubject = PublishSubject.create<Int>()
        val pair = thisDishId to newSubject
        val foo = newSubject.subscribe { value ->
          Log.i(TAG, "Subscribe value $value $thisDishId")
          currentValueMap[thisDishId] = value
          setHowManyServingsTV(value)
          setPriceData(value, list[position].dishModel.dishId)
          setIngredientList(position)
        }
        disposables += disposables + (thisDishId to foo)
        newSubject.onNext(1)
        amountOfServingsSubjects = amountOfServingsSubjects + pair
      } else {
        val value = currentValueMap.getValue(list[position].dishModel.dishId)
        setHowManyServingsTV(value)
        setPriceData(value, list[position].dishModel.dishId)
      }
    }

    private val positionIncludedAdsBinded get() = adCase.correctElementFromListToBind(this.adapterPosition)

    fun bind() {
      Log.i(TAG, "bind, pos: $positionIncludedAdsBinded")
      if (positionIncludedAdsBinded > list.size) {
        Log.e("TEST", "position smaller than list")
        return
      }
      setDishData(positionIncludedAdsBinded)
      sumPriceAndSetPriceData(positionIncludedAdsBinded)
      createSubjectIfDoesNotExist(positionIncludedAdsBinded)
      setIngredientList(positionIncludedAdsBinded)
      if (positionIncludedAdsBinded == 6) openFeedBackForm()
      setNameTaxAndMarginAccordingly(positionIncludedAdsBinded)
      setButtons(positionIncludedAdsBinded)
      makeDishCardOpenable(positionIncludedAdsBinded)
      setHowManyServingsTvAsButton(
        viewBinding.howManyServingsTextView,
        positionIncludedAdsBinded
      )
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
        openCreateNewDishDialog()
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
    if (holder.itemViewType == DISH_ITEM_TYPE) (holder as DishRecyclerViewHolder).bind()
    else if (holder.itemViewType == DISH_AD_ITEM_TYPE) (holder as AdItemViewHolder).bind()
    else (holder as LastItemViewHolder).bind()
  }

  private fun openDialog(dialog: DialogFragment) {
    val transaction = fragmentManager.beginTransaction()
    transaction.addToBackStack(dialog.tag)
    dialog.show(transaction, TAG)
  }


  private fun openFeedBackForm() {
    val manager = ReviewManagerFactory.create(activity)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { thisRequest ->
      if (thisRequest.isSuccessful) {
        val reviewInfo = thisRequest.result
        val flow = manager.launchReviewFlow(activity, reviewInfo)
        flow.addOnCompleteListener {
          Log.i(TAG, "review success!")
        }
      } else {
        Log.i(TAG, "review fail!")
      }
    }
  }

  companion object {
    const val TAG = "DishesFragmentRV"
  }

  private fun isThisLastItemOfTheList(indexOfHalfProduct: Int, listSize: Int): Boolean {
    return (indexOfHalfProduct == listSize - 1)
  }
}
