package com.erdees.foodcostcalc.ui.dialogFragments.areYouSureFragment

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
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment.EditDishFragment
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment.EditHalfProductFragment
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.editProductDialogFragment.EditProductFragment
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel


/**
 *
 * */
class AreYouSure : DialogFragment() {

    private lateinit var viewModel: AreYouSureViewModel
    private lateinit var dishModelToDelete: DishModel
    private lateinit var productModelToDelete: ProductModel
    private lateinit var halfProductModelToDelete: HalfProductModel

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

        /**Position to delete, after pushing delete button, information about product/dishModel index in main list
         * is passed to this variable.*/
        var pos: Int? = null

        /**List of products included, initially empty, gets populated by every product included in dishModel
         * that is about to get deleted.*/
        var listOfProductsIncludedToErase = listOf<ProductIncluded>()

        /**Same as above*/
        var listOfProductsIncludedInHalfProductToErase = listOf<ProductIncludedInHalfProductModel>()

        var listOfHalfProductsIncludedInDish = listOf<HalfProductIncludedInDishModel>()

        /**Observe data to set positions to provide parameters for delete methods */
        viewModel.getPosition().observe(viewLifecycleOwner, { position ->
            pos = position
        })


        /** Get dishModel to delete only if this was called from EditDishFragment.
         * Populates 'listOfProductsIncludedToErase' with list of ProductsIncluded in
         * DishModel that is saved as 'dishModelToDelete' so they will be erased from database.
         * */
        viewModel.readAllDishModelData.observe(viewLifecycleOwner, { dish ->
            if (this.tag == EditDishFragment.TAG) {
                dishModelToDelete = dish[pos!!]
                viewModel.getProductIncludedByDishId(dishModelToDelete.dishId)
                    .observe(viewLifecycleOwner, Observer { listOfProductsIncluded ->
                        listOfProductsIncludedToErase = listOfProductsIncluded
                    })
            }
        })
        /**Same as above but with ProductsFragment included in half product
         * +
         * Get list of every productincluded in half product
         * +
         *  Get list of every halfProductModel Included in dishModel in order to erase them from database
         * when certain HalfProductModel gets erased.
         * */
        viewModel.readAllHalfProductModelData.observe(viewLifecycleOwner, { halfProduct ->
            if (this.tag == EditHalfProductFragment.TAG) {
                halfProductModelToDelete = halfProduct[pos!!]
                viewModel.getProductsIncludedFromHalfProduct(halfProductModelToDelete.halfProductId)
                    .observe(viewLifecycleOwner, Observer { listOfProductsIncludedInHalfProduct ->
                        listOfProductsIncludedInHalfProductToErase =
                            listOfProductsIncludedInHalfProduct
                    })
                viewModel.getHalfProductsIncludedInDishFromDishByHalfProduct(
                    halfProductModelToDelete.halfProductId
                )
                    .observe(viewLifecycleOwner, {
                        listOfHalfProductsIncludedInDish = it
                    })
            }
        })

        /**Get product to delete only if this was called from EditProductFragment.*/
        viewModel.readAllProductModelData.observe(viewLifecycleOwner, { product ->
            if (this.tag == EditProductFragment.TAG) productModelToDelete = product[pos!!]
        })

        /**Get Half product to delete only if this was called from EditHalfProductFragment*/
        viewModel.readAllHalfProductModelData.observe(viewLifecycleOwner, { halfProduct ->
            if (this.tag == EditHalfProductFragment.TAG) halfProductModelToDelete =
                halfProduct[pos!!]
        })


        /**Function, made because of inconsistency in deleting products included associated with dishModel* */
        fun deleteAllProductIncluded(list: List<ProductIncluded>) {
            list.forEach { viewModel.deleteProductIncluded(it) }
            var listOfSurvivors = listOf<ProductIncluded>()
            viewModel.getProductIncludedByDishId(dishModelToDelete.dishId)
                .observe(viewLifecycleOwner, Observer { listOfProducts ->
                    listOfSurvivors = listOfProducts
                })
            if (listOfSurvivors.isNotEmpty()) deleteAllProductIncluded(list)
        }

        /** Button logic: tag informs this dialog from where it was open so it knows what action to proceed.*
         *  listOfProductsIncludedToErase is method which deletes every trace of dishModel in ProductIncluded query,
         *  so the database is neat and organized. --- It doesn't work 100% effectively and I can't see reason
         *  why just yet but this needs to be refactored somehow
         *
         *  Edit : Added a Thread.sleep(100) in order to give more time for viewmodel function work,
         *  apparently the reason why not every product was deleted from dishModel is that this dialog got dismissed
         *  before the function finished it action, which I don't completely understand because I even used recursive
         *  function to make sure it's done before dialog gets dismissed. Anyways, now everything works properly even
         *  when there's bigger amount of products included in dishModel which is fine.
         */

        confirmBtn.setOnClickListener {
            when (this.tag) {
                EditProductFragment.TAG -> {
                    viewModel.setFlag(false) //flag provides an information to fragment that this object was just deleted and fragment should close itself
                    viewModel.deleteProduct(productModelToDelete)
                }
                EditDishFragment.TAG -> {
                    viewModel.setFlag(false)
                    deleteAllProductIncluded(listOfProductsIncludedToErase)
                    viewModel.deleteDish(dishModelToDelete)
                }
                EditHalfProductFragment.TAG -> {
                    viewModel.setFlag(false)
                    listOfProductsIncludedInHalfProductToErase.forEach {
                        viewModel.deleteProductIncludedInHalfProduct(
                            it
                        )
                    }
                    viewModel.deleteHalfProducts(halfProductModelToDelete) // Delete every halfProductIncludedInDish
                    listOfHalfProductsIncludedInDish.forEach {
                        viewModel.deleteHalfProductIncludedInDish(
                            it
                        )
                    }
                }

                "EditDishFragmentRecyclerAdapter" -> {
                    viewModel.getDishesWithProductsIncluded()
                        .observe(
                            viewLifecycleOwner,
                            { viewModel.deleteProductIncluded(viewModel.getProductIncluded().value!!) })
                }
                "EditHalfProductFragmentRecyclerAdapter" -> {
                    viewModel.getHalfProductWithProductIncluded()
                        .observe(
                            viewLifecycleOwner,
                            {
                                viewModel.deleteProductIncludedInHalfProduct(
                                    viewModel.getProductIncludedInHalfProduct().value!!
                                )
                            }
                        )
                }
                "EditDishAdapterDeleteHalfProduct" -> {
                    viewModel.getHalfProductIncluded().observe(viewLifecycleOwner,
                         { halfProductIncluded ->
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