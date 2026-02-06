package com.bruno.shoppinglist

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.database.database

class ShoppingApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)
    }
}