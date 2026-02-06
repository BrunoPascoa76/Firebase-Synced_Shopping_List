package com.bruno.shoppinglist.data

data class ShoppingListItem (
    val name: String = "",
    val quantity: Int = 1,
    val isPurchased: Boolean = false
)
