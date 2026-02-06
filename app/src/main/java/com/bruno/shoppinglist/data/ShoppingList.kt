package com.bruno.shoppinglist.data

data class ShoppingList(
    val name: String = "",
    val ownerId: String = "",
    val categories: Map<String, ShoppingListCategory> = emptyMap()
)