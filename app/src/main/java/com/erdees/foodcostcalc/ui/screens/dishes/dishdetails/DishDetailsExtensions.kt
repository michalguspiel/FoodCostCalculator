package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

/**
 * Extension function to create DishDetailsScreenActions from the DishDetailsViewModel
 * and required callbacks.
 */
fun DishDetailsViewModel.createActions(
    getCopyDishPrefilledName: (String?) -> String
): DishDetailsScreenActions {
    return DishDetailsScreenActions(
        dishActions = DishActions(
            saveDish = ::saveDish,
            shareDish = ::shareDish,
            saveAndNavigate = ::saveAndNavigate,
            resetScreenState = ::resetScreenState
        ),
        propertyActions = DishPropertyActions(
            updateName = ::updateName,
            saveName = ::saveDishName,
            updateTax = ::updateTax,
            saveTax = ::saveDishTax,
            updateMargin = ::updateMargin,
            saveMargin = ::saveDishMargin,
            updateTotalPrice = ::updateTotalPrice,
            saveTotalPrice = ::saveDishTotalPrice
        ),
        itemActions = ItemActions(
            removeItem = ::removeItem,
            updateQuantity = ::updateQuantity,
            saveQuantity = ::updateItemQuantity,
            setComponentSelection = ::setComponentSelection,
            onAddExistingComponentClick = ::onAddExistingComponent
        ),
        deletionActions = DishDeletionActions(
            onDeleteDishClick = ::onDeleteDishClick,
            onDeleteConfirmed = ::confirmDelete
        ),
        copyActions = DishCopyActions(
            onCopyDishClick = { handleCopyDish { getCopyDishPrefilledName(it) } },
            copyDish = ::copyDish,
            updateCopiedDishName = ::updateCopiedDishName,
            hideCopyConfirmation = ::hideCopyConfirmation
        ),
        interactionActions = ScreenInteractionActions(
            setInteraction = ::setInteraction,
            saveChangesAndProceed = ::saveChangesAndProceed,
            discardChangesAndProceed = { discardChangesAndProceed { getCopyDishPrefilledName(it) } }
        )
    )
}