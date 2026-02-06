package com.bruno.shoppinglist.repositories

import android.util.Log
import com.bruno.shoppinglist.data.ShoppingList
import com.bruno.shoppinglist.data.ShoppingListPreview
import com.bruno.shoppinglist.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.database.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val userId: String) {
    private val db= Firebase.database.reference
    private val userRef = db.child("users").child(userId)


    init{
        userRef.keepSynced(true)
    }

    suspend fun createUserIfMissing(){
        val snapshot = userRef.get().await()

        if(!snapshot.exists()){
            val authUser= Firebase.auth.currentUser
            val newUser= User(authUser?.displayName ?: "", myLists = emptyMap())
            userRef.setValue(newUser).await()
            Log.d("UserRepository", "User created successfully")
        }else{
            Log.d("UserRepository", "User already exists")
        }
    }

    fun observeUser(): Flow<User?> {
        return userRef.snapshots.map { snapshot ->
            snapshot.getValue(User::class.java)
        }
    }

    fun createShoppingList(name: String){
        val listId=db.child("shopping_lists").push().key?:return
        val newShoppingListPreview= ShoppingListPreview(name = name)
        val newShoppingList= ShoppingList(name = name, ownerId = userId, categories = emptyMap())
        val updates = mapOf(
            "users/$userId/myLists/$listId" to true,       // Links the list to the user
            "shopping_lists/$listId" to newShoppingList    // Creates the actual data node
        )

        db.updateChildren(updates).addOnFailureListener { error ->
            Log.e("UserRepository", "Error creating shopping list: ${error.message}")
        }
    }

    fun importShoppingList(listId: String){
        userRef.child("myLists").child(listId).setValue(true)
        Log.d("UserRepository", "Imported shopping list with ID: $listId")
    }

    fun deleteShoppingList(listId: String){
        userRef.child("myLists").child(listId).removeValue()
        Log.d("UserRepository", "Deleted shopping list with ID: $listId")
    }

}