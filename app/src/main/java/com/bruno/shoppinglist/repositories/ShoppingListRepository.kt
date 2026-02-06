package com.bruno.shoppinglist.repositories

import com.bruno.shoppinglist.data.ShoppingList
import com.bruno.shoppinglist.data.ShoppingListCategory
import com.bruno.shoppinglist.data.ShoppingListItem
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.database.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShoppingListRepository(private val listId: String) {
    private val db= Firebase.database.reference

    private val shoppingListRef=db.child("shopping_lists").child(listId)

    init{
        shoppingListRef.keepSynced(true)
    }

    fun observeList(): Flow<ShoppingList?> {
        return shoppingListRef.snapshots.map { snapshot ->
            snapshot.getValue(ShoppingList::class.java)
        }
    }

    fun changeName(newName: String){
        shoppingListRef.child("name").setValue(newName)
    }

    fun addCategory(name: String="General"){
        val categoryId=shoppingListRef.child("categories").push().key?:return
        val newCategory= ShoppingListCategory(
            name = name,
            items = emptyMap()
        )
        shoppingListRef.child("categories").child(categoryId).setValue(newCategory)
    }

    fun deleteCategory(categoryId: String) {
        shoppingListRef.child("categories").child(categoryId).removeValue()
    }

    fun modifyCategory(categoryId: String, newName: String) {
        shoppingListRef.child("categories").child(categoryId).child("name").setValue(newName)
    }

    fun addItem(categoryId: String, name: String, quantity: Int=1){
        val itemId=shoppingListRef.child("categories").child(categoryId).child("items").push().key?:return
        val newItem= ShoppingListItem(
            name = name,
            quantity = quantity,
            isPurchased = false
        )

        shoppingListRef.child("categories").child(categoryId).child("items").child(itemId).setValue(newItem)
    }

    fun deleteItem(categoryId: String, itemId: String){
        shoppingListRef.child("categories").child(categoryId).child("items").child(itemId).removeValue()
    }

    fun modifyItem(categoryId: String, itemId: String, updates: Map<String, Any>){
        shoppingListRef.child("categories").child(categoryId).child("items").child(itemId).updateChildren(updates)
    }
}