package com.bruno.shoppinglist.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bruno.shoppinglist.data.ShoppingList
import com.bruno.shoppinglist.repositories.ShoppingListRepository
import com.bruno.shoppinglist.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllListsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
): ViewModel() {
    init {
        viewModelScope.launch {
            userRepository.createUserIfMissing() //just to make sure
        }
    }

    val shoppingListIds: StateFlow<List<String>> = userRepository.observeUser()
        .map { it?.myLists?.keys?.toList() ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeList(id: String) = userRepository.deleteShoppingList(id)

    fun createList(name: String) = userRepository.createShoppingList(name)

    fun importList(id: String) = userRepository.importShoppingList(id)

    fun renameList(id: String, newName: String) = shoppingListRepository.changeName(id, newName)

    fun getList(id: String): Flow<ShoppingList?> = shoppingListRepository.observeList(id)
}