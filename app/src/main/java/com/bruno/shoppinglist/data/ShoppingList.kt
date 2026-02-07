package com.bruno.shoppinglist.data

data class ShoppingList(
    val name: String = "",
    val ownerId: String = "",
    val users: Int=1, //how many people are using it, for safe deletion
    val categories: Map<String, ShoppingListCategory> = emptyMap()
)