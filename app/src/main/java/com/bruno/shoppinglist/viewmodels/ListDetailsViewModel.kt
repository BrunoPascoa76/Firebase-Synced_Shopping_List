package com.bruno.shoppinglist.viewmodels

import androidx.lifecycle.ViewModel
import com.bruno.shoppinglist.data.ShoppingList
import com.bruno.shoppinglist.repositories.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ListDetailsViewModel @Inject constructor(
    private val repository: ShoppingListRepository
): ViewModel(){
    fun getList(listId: String): Flow<ShoppingList?> {
        return repository.observeList(listId)
    }
}