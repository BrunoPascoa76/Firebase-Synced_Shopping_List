package com.bruno.shoppinglist.modules

import com.bruno.shoppinglist.repositories.ShoppingListRepository
import com.bruno.shoppinglist.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    @Provides
    @Singleton
    fun provideUserRepository(userId: String): UserRepository {
        return UserRepository(userId)
    }

    @Provides
    @Singleton
    fun provideShoppingListRepository(): ShoppingListRepository {
        return ShoppingListRepository()
    }
}