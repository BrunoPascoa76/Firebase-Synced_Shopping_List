package com.bruno.shoppinglist.data

data class ShoppingListItem (
    val name: String = "",
    val quantity: Int = 1,
    val purchased: Boolean = false,
    val position: Double = 0.0
)
