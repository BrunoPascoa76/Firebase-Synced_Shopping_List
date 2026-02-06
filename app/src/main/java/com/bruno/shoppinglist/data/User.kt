package com.bruno.shoppinglist.data

data class User (
    val myLists: Map<String, ShoppingListPreview> = emptyMap()
)