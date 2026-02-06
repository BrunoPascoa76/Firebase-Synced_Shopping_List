package com.bruno.shoppinglist.data

//by storing a preview, we can display all lists the user with their names has but only fetch the one he actually needs at the moment
data class ShoppingListPreview(
    val name: String = ""
)
