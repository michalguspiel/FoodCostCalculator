package com.example.foodcostcalc.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.R
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.viewmodel.AddViewModel

class AreYouSure : DialogFragment() {

    private lateinit var viewModel: AddViewModel
    private lateinit var dishToDelete: Dish
    private lateinit var productToDelete: Product

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_dialog_are_you_sure, container, false)

        /** initialize ui with viewmodel*/
        viewModel = ViewModelProvider(this).get(AddViewModel::class.java)

        /** Binders*/
        val confirmBtn = view.findViewById<Button>(R.id.button_yes)
        val cancelBtn = view.findViewById<Button>(R.id.button_cancel)

        /**Position to delete, after pushing delete button, information about product/dish index in main list
         * is passed to this variable.*/
        var pos: Int? = null

        /**List of products included, initially empty, gets populated by every product included in dish
         * that is about to get deleted.*/
        var listOfProductsIncludedToErase = listOf<ProductIncluded>()

        /**Observe data to set positions to provide parameters for delete methods */
        viewModel.getPosition().observe(viewLifecycleOwner, Observer { position ->
            pos = position
        })

        /** Get dish to delete only if this was called from EditDish.
         * Populates 'listOfProductsIncludedToErase' with list of ProductsIncluded in
         * Dish that is saved as 'dishToDelete' so they will be erased from database.
         * */
        viewModel.getDishes().observe(viewLifecycleOwner, Observer { dish ->
            if (this.tag == EditDish.TAG) {
                dishToDelete = dish[pos!!]
                viewModel.getProductIncludedByDishId(dishToDelete.dishId)
                    .observe(viewLifecycleOwner, Observer { listOfProductsIncluded ->
                        listOfProductsIncludedToErase = listOfProductsIncluded
                    })
            }
        })

        /**Get product to delete only if this was called from EditProduct.*/
        viewModel.getProducts().observe(viewLifecycleOwner, Observer { product ->
            if (this.tag == EditProduct.TAG) productToDelete = product[pos!!]
        })


        /**Function, made because of inconsistency in deleting products included associated with dish
          Even though its recursive it still doesn't work properly* */
        fun deleteAllProductIncluded(list: List<ProductIncluded>) {
            list.forEach { viewModel.deleteProductIncluded(it) }
            var listOfSurvivors = listOf<ProductIncluded>()
            viewModel.getProductIncludedByDishId(dishToDelete.dishId)
                .observe(viewLifecycleOwner, Observer { listOfProducts ->
                    listOfSurvivors = listOfProducts
                })
            if (listOfSurvivors.isNotEmpty()) deleteAllProductIncluded(list)
        }

        /** Button logic: tag informs this dialog from where it was open so it knows what action to proceed.*
         *  listOfProductsIncludedToErase is method which deletes every trace of dish in ProductIncluded query,
         *  so the database is neat and organized. --- It doesn't work 100% effectively and I can't see reason
         *  why just yet but this needs to be refactored somehow
         *
         *  Edit : Added a Thread.sleep(100) in order to give more time for viewmodel function work,
         *  apparently the reason why not every product was deleted from dish is that this dialog got dismissed
         *  before the function finished it action, which I don't completely understand because I even used recursive
         *  function to make sure it's done before dialog gets dismissed. Anyways, now everything works properly even
         *  when there's bigger amount of products included in dish which is fine.
         */

        confirmBtn.setOnClickListener {
            viewModel.setFlag(false)
            when (this.tag) {
                EditProduct.TAG -> viewModel.deleteProduct(productToDelete)
                EditDish.TAG -> {
                    deleteAllProductIncluded(listOfProductsIncludedToErase)
                    viewModel.deleteDish(dishToDelete)
                }
                "EditDishAdapter" -> viewModel.getDishesWithProductsIncluded()
                    .observe(
                        viewLifecycleOwner,
                        Observer { viewModel.deleteProductIncluded(viewModel.getProductIncluded().value!!) })
                else -> this.dismiss()
            }
            Thread.sleep(100) // This is here because otherwise dialog gets closed before viewmodel functions are called
            this.dismiss()
        }


        cancelBtn.setOnClickListener {
            this.dismiss()
        }



        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    companion object {
        fun newInstance(): AreYouSure =
            AreYouSure()

        const val TAG = "AreYouSure"

    }
}