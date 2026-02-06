package com.bruno.shoppinglist.data

data class ShoppingListCategory (
    val name: String = "General",
    val items: Map<String,ShoppingListItem> = emptyMap()
)