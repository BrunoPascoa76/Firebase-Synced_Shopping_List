package com.bruno.shoppinglist.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomeScreen(user: FirebaseUser){
    Text("you're in ${user.displayName}")
}