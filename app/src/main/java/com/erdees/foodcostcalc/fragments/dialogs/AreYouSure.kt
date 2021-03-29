package com.erdees.foodcostcalc.fragments.dialogs

import android.content.DialogInterface
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
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.model.*
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.erdees.foodcostcalc.viewmodel.AreYouSureViewModel
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel

class AreYouSure : DialogFragment() {

    private lateinit var viewModel: AreYouSureViewModel
    private lateinit var dishToDelete: Dish
    private lateinit var productToDelete: Product
    private lateinit var halfProductToDelete : HalfProduct

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_dialog_are_you_sure, container, false)

        /** initialize ui with viewmodel*/
        viewModel = ViewModelProvider(this).get(AreYouSureViewModel::class.java)

        /** Binders*/
        val confirmBtn = view.findViewById<Button>(R.id.button_yes)
        val cancelBtn = view.findViewById<Button>(R.id.button_cancel)

        /**Position to delete, after pushing delete button, information about product/dish index in main list
         * is passed to this variable.*/
        var pos: Int? = null

        /**List of products included, initially empty, gets populated by every product included in dish
         * that is about to get deleted.*/
        var listOfProductsIncludedToErase = listOf<ProductIncluded>()
        /**Same as above*/
        var listOfProductsIncludedInHalfProductToErase = listOf<ProductIncludedInHalfProduct>()

        var listOfHalfProductsIncludedInDish = listOf<HalfProductIncludedInDish>()

        /**Observe data to set positions to provide parameters for delete methods */
        viewModel.getPosition().observe(viewLifecycleOwner, Observer { position ->
            pos = position
        })


        /** Get dish to delete only if this was called from EditDish.
         * Populates 'listOfProductsIncludedToErase' with list of ProductsIncluded in
         * Dish that is saved as 'dishToDelete' so they will be erased from database.
         * */
        viewModel.readAllDishData.observe(viewLifecycleOwner, Observer { dish ->
            if (this.tag == EditDish.TAG) {
                dishToDelete = dish[pos!!]
                viewModel.getProductIncludedByDishId(dishToDelete.dishId)
                    .observe(viewLifecycleOwner, Observer { listOfProductsIncluded ->
                        listOfProductsIncludedToErase = listOfProductsIncluded
                    })
            }
        })
        /**Same as above but with Products included in half product
         * +
         * Get list of every productincluded in half product
         * +
         *  Get list of every halfProduct Included in dish in order to erase them from database
         * when certain HalfProduct gets erased.
         * */
        viewModel.readAllHalfProductData.observe(viewLifecycleOwner, Observer { halfProduct ->
            if(this.tag == EditHalfProduct.TAG){
                halfProductToDelete = halfProduct[pos!!]
                viewModel.getProductsIncludedFromHalfProduct(halfProductToDelete.halfProductId)
                    .observe(viewLifecycleOwner, Observer { listOfProductsIncludedInHalfProduct ->
                        listOfProductsIncludedInHalfProductToErase = listOfProductsIncludedInHalfProduct
                    })
                viewModel.getHalfProductsIncludedInDishFromDishByHalfProduct(halfProductToDelete.halfProductId)
                    .observe(viewLifecycleOwner,{
                        listOfHalfProductsIncludedInDish = it
                    })
            }
        })

        /**Get product to delete only if this was called from EditProduct.*/
        viewModel.readAllProductData.observe(viewLifecycleOwner,  { product ->
            if (this.tag == EditProduct.TAG) productToDelete = product[pos!!]
        })

        /**Get Half product to delete only if this was called from EditHalfProduct*/
        viewModel.readAllHalfProductData.observe(viewLifecycleOwner,  { halfProduct ->
            if(this.tag == EditHalfProduct.TAG)  halfProductToDelete = halfProduct[pos!!]
        })


        /**Function, made because of inconsistency in deleting products included associated with dish* */
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
            when (this.tag) {
                EditProduct.TAG -> {
                    viewModel.setFlag(false) //flag provides an information to fragment that this object was just deleted and fragment should close itself
                    viewModel.deleteProduct(productToDelete)
                }
                EditDish.TAG -> {
                    viewModel.setFlag(false)
                    deleteAllProductIncluded(listOfProductsIncludedToErase)
                    viewModel.deleteDish(dishToDelete)
                }
                EditHalfProduct.TAG ->{
                    viewModel.setFlag(false)
                    listOfProductsIncludedInHalfProductToErase.forEach { viewModel.deleteProductIncludedInHalfProduct(it)}
                    viewModel.deleteHalfProducts(halfProductToDelete)
                    // Delete every halfProductIncludedInDish
                    listOfHalfProductsIncludedInDish.forEach { viewModel.deleteHalfProductIncludedInDish(it) }
                }

                "EditDishAdapter" -> {
                    viewModel.getDishesWithProductsIncluded()
                        .observe(
                            viewLifecycleOwner,
                            Observer { viewModel.deleteProductIncluded(viewModel.getProductIncluded().value!!) })
                }
                "EditHalfProductAdapter" -> {
                    viewModel.getHalfProductWithProductIncluded()
                        .observe(
                            viewLifecycleOwner,
                            Observer {
                                viewModel.deleteProductIncludedInHalfProduct(
                                    viewModel.getProductIncludedInHalfProduct().value!!
                                )
                            }
                        )
                }
                "EditDishAdapterDeleteHalfProduct" -> {
                    viewModel.getHalfProductIncluded().observe(viewLifecycleOwner,
                        Observer { halfProductIncluded ->
                            viewModel.deleteHalfProductIncludedInDish(halfProductIncluded)
                        })
                }
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