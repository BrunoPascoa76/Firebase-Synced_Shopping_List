package com.bruno.shoppinglist.data

data class User (
    val name: String = "",
    val myLists: Map<String, Boolean> = emptyMap() //the boolean is just to keep a value
)