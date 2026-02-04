package com.bruno.shoppinglist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomeScreen(user: FirebaseUser){
    Text("you're in ${user.displayName}")
}