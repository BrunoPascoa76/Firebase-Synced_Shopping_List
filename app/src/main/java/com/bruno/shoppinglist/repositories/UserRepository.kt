package com.bruno.shoppinglist.repositories

import android.util.Log
import com.bruno.shoppinglist.data.ShoppingList
import com.bruno.shoppinglist.data.ShoppingListPreview
import com.bruno.shoppinglist.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
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
            .addOnSuccessListener {
                val listUsersRef = db
                    .child("shopping_lists")
                    .child(listId)
                    .child("users")

                listUsersRef.setValue(ServerValue.increment(1))
                    .addOnSuccessListener {
                        Log.d("UserRepository", "List $listId imported and counter incremented")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserRepository", "Failed to increment counter", e)
                    }
            }

        Log.d("UserRepository", "Started import for list ID: $listId")
    }

    fun deleteShoppingList(listId: String) {
        userRef.child("myLists").child(listId).removeValue()

        val listRef = db.child("shopping_lists").child(listId)

        listRef.child("users").setValue(ServerValue.increment(-1))

        listRef.child("users").get().addOnSuccessListener { snapshot ->
            val count = snapshot.getValue(Int::class.java) ?: 0
            if (count <= 0) {
                listRef.removeValue()
            }
        }
    }
}