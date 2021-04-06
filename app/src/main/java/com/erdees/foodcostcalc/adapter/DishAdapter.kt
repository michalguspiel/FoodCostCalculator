package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.calculatePrice
import com.erdees.foodcostcalc.formatPrice
import com.erdees.foodcostcalc.fragments.dialogs.AddProductToDish
import com.erdees.foodcostcalc.fragments.dialogs.EditDish
import com.erdees.foodcostcalc.model.GrandDish
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.DishAdapterViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.DishListViewAdapterViewModel
import com.erdees.foodcostcalc.views.MaskedItemView
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.ArrayList


class DishAdapter(
    val tag: String?,
    private val list: ArrayList<GrandDish>,
    private val fragmentManager: FragmentManager,
    val viewModel: DishAdapterViewModel,
    private val dishListViewAdapterViewModel : DishListViewAdapterViewModel,
    val viewLifecycleOwner: LifecycleOwner,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val LAST_ITEM_TYPE = 0
    private val DISH_ITEM_TYPE = 1


    inner class DishRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val eachLinearLayout: LinearLayout = view.findViewById(R.id.linear_layout_dish_card)
        private val dishNameTextView: TextView = view.findViewById(R.id.dish_name_in_adapter)
        private val dishMarginTextView: TextView = view.findViewById(R.id.dish_margin_in_adapter)
        private val dishTaxTextView: TextView = view.findViewById(R.id.dish_tax_in_adapter)
        private val editButton: ImageButton = view.findViewById(R.id.edit_button_in_dish_adapter)
        private val addProductsButton : ImageButton = view.findViewById(R.id.add_product_to_dish_button)
        private val listView: ListView = view.findViewById(R.id.list_view)
        private val totalPriceOfDish: TextView = view.findViewById(R.id.total_price_dish_card_view)
        private val finalPriceWithMarginAndTax: TextView =
            view.findViewById(R.id.total_price_with_margin_dish_card_view)
        var totalPrice: Double = 0.0

        fun bind(position: Int){
            if(position == 5){
                Log.i("Main Activity", "Tried to open feedback form")
                openFeedBackForm()
            }

            /**Computes height of listView based on each row height, includes dividers.
             * I'm using this approach so listView size is set and doesn't need to be scrollable. */
            fun getListSize(): Int {
                var result = 0
                for (eachProduct in list[position].productsIncluded.indices + // first products included
                        list[position].halfProducts.indices) { // plus halfProducts
                    val listItem = listView.adapter.getView(eachProduct, null, listView)
                    listItem.measure(0, View.MeasureSpec.UNSPECIFIED)
                    result += listItem.measuredHeight
                }
                return result + (listView.dividerHeight * (listView.adapter.count - 1))
            }


            fun countPriceAfterMarginAndTax(number: Double): Double {
                val priceWithMargin = number * list[position].dish.marginPercent / 100
                val amountOfTax = priceWithMargin * list[position].dish.dishTax / 100
                return priceWithMargin + amountOfTax
            }

            /**Summing up total price of products included and then one by one adding price of each half product,
             * after each call formats total price and sets totalPriceOfDish and finalPriceWithMarginAndTax*/

            /**To set correct price when there's no halfproducts.*/
            totalPrice = list[position].totalPrice
            totalPriceOfDish.text = formatPrice(totalPrice)
            finalPriceWithMarginAndTax.text = formatPrice(countPriceAfterMarginAndTax(totalPrice))

            list[position].halfProducts.forEach {
                viewModel
                    .getCertainHalfProductWithProductsIncluded(it.halfProductOwnerId)
                    .observe(viewLifecycleOwner, Observer { halfProductWithProductsIncluded ->
                        totalPrice = (totalPrice +
                                calculatePrice(halfProductWithProductsIncluded.pricePerUnit(),it.weight,
                                    halfProductWithProductsIncluded.halfProduct.halfProductUnit,it.unit))
                        if(list[position].halfProducts.indexOf(it) == list[position].halfProducts.size - 1)//Text fields are changed only when price of last half product is added to total price.
                        {
                            totalPriceOfDish.text =  formatPrice(totalPrice)
                           finalPriceWithMarginAndTax.text = formatPrice(countPriceAfterMarginAndTax(totalPrice))
                        }
                    })

            }
           dishNameTextView.text = list[position].dish.name
            dishMarginTextView.text = "Margin: ${list[position].dish.marginPercent}%"
            dishTaxTextView.text = "Tax: ${list[position].dish.dishTax}%"
            editButton.setOnClickListener {
                EditDish().show(fragmentManager, EditDish.TAG)
                EditDish.dishPassedFromAdapter = list[position]
            }

            addProductsButton.setOnClickListener{
                viewModel.passDishToDialog(list[position].dish)
                openDialog(AddProductToDish())
            }

            /**Opening/closing list after click*/
            eachLinearLayout.setOnClickListener {
                if (listView.adapter == null) {
                    listView.adapter = makeAdapterForList(position)
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

    inner class LastItemViewHolder(view:View) : RecyclerView.ViewHolder(view){
        private val layoutAsButton: MaskedItemView = view.findViewById(R.id.products_last_item_layout)
        private val text : TextView = view.findViewById(R.id.last_item_text)

        fun bind(){
            text.text = "Create Dish"
            layoutAsButton.setOnClickListener {
                Log.i("TEST","WORKS")
                viewModel.setOpenCreateDishFlag(true)
                viewModel.setOpenCreateDishFlag(false)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DISH_ITEM_TYPE -> {
                val adapterLayout =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.dish_card_view, parent, false)
                DishRecyclerViewHolder(adapterLayout)
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
        return list.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            list.size -> LAST_ITEM_TYPE
            else -> DISH_ITEM_TYPE
        }
    }

    @SuppressLint("WrongConstant", "ShowToast", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == DISH_ITEM_TYPE) (holder as DishRecyclerViewHolder).bind(position)
        else (holder as LastItemViewHolder).bind()
    }


    private fun openDialog(dialog: DialogFragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.addToBackStack(dialog.tag)
        dialog.show(transaction, TAG)
    }

    private fun  makeAdapterForList( position: Int): ListAdapter{
      return DishListViewAdapter(
            activity,
            list[position],
            dishListViewAdapterViewModel,
            viewLifecycleOwner
        )
    }


    private fun openFeedBackForm(){
        Log.i("Main Activity", "review triggered!")
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                val reviewInfo = request.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    //Continue your application process
                    Log.i("Main Activity", "review success!")
                }
            } else {
                //Handle the error here
                Log.i("Main Activity", "review fail!")

            }
        }
    }

    companion object {
        const val TAG = "DishAdapter"
    }
}