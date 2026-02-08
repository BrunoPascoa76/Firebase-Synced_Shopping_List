package com.bruno.shoppinglist.repositories

import com.bruno.shoppinglist.data.ShoppingList
import com.bruno.shoppinglist.data.ShoppingListCategory
import com.bruno.shoppinglist.data.ShoppingListItem
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.database.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShoppingListRepository {
    private val db= Firebase.database.reference

    private val shoppingListRef=db.child("shopping_lists")

    init{
        shoppingListRef.keepSynced(true)
    }

    fun observeList(listId: String): Flow<ShoppingList?> {
        return shoppingListRef.child(listId).snapshots.map { snapshot ->
            snapshot.getValue(ShoppingList::class.java)
        }
    }

    fun changeName(listId: String, newName: String){
        shoppingListRef.child(listId).child("name").setValue(newName)
    }

    fun addCategory(listId:String, name: String="General"){
        val categoryId=shoppingListRef.child(listId).child("categories").push().key?:return
        val newCategory= ShoppingListCategory(
            name = name,
            items = emptyMap()
        )
        shoppingListRef.child(listId).child("categories").child(categoryId).setValue(newCategory)
    }

    fun deleteCategory(listId:String, categoryId: String) {
        shoppingListRef.child(listId).child("categories").child(categoryId).removeValue()
    }

    fun modifyCategory(listId:String, categoryId: String, newName: String) {
        shoppingListRef.child(listId).child("categories").child(categoryId).child("name").setValue(newName)
    }

    fun addItem(listId:String, categoryId: String, name: String, quantity: Int=1){
        val itemId=shoppingListRef.child(listId).child("categories").child(categoryId).child("items").push().key?:return
        val newItem= ShoppingListItem(
            name = name,
            quantity = quantity,
            purchased = false
        )

        shoppingListRef.child(listId).child("categories").child(categoryId).child("items").child(itemId).setValue(newItem)
    }

    fun deleteItem(listId:String, categoryId: String, itemId: String){
        shoppingListRef.child(listId).child("categories").child(categoryId).child("items").child(itemId).removeValue()
    }

    fun modifyItem(listId:String, categoryId: String, itemId: String, updates: Map<String, Any>){
        shoppingListRef.child(listId).child("categories").child(categoryId).child("items").child(itemId).updateChildren(updates)
    }
}