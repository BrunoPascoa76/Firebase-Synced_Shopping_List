package com.bruno.shoppinglist.viewmodels

import androidx.lifecycle.ViewModel
import com.bruno.shoppinglist.data.ShoppingList
import com.bruno.shoppinglist.data.ShoppingListItem
import com.bruno.shoppinglist.repositories.ShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ListDetailsViewModel @Inject constructor(
    private val repository: ShoppingListRepository
): ViewModel(){
    fun getList(listId: String): Flow<ShoppingList?> {
        return repository.observeList(listId)
    }

    fun createCategory(listId:String, name:String){
        repository.addCategory(listId,name)
    }

    fun createItem(listId:String,categoryId:String,name:String,quantity:Int){
        repository.addItem(listId,categoryId,name,quantity)
    }

    fun toggleItemPurchased(listId:String,categoryId: String,itemId:String,purchased: Boolean){
        repository.modifyItem(listId,categoryId,itemId, updates = mapOf("purchased" to purchased))
    }

    fun deleteItem(listId: String,categoryId: String,itemId: String){
        repository.deleteItem(listId,categoryId,itemId)
    }

    fun deleteCategory(listId: String,categoryId: String){
        repository.deleteCategory(listId,categoryId)
    }

    fun moveItem(
        listId: String,
        categoryId: String,
        itemId: String,
        newIndex: Int,
        currentItems: List<Pair<String, ShoppingListItem>>
    ) {
        if (currentItems.isEmpty()) return

        val newPos: Double = when {
            currentItems.size <= 1 -> 0.0
            newIndex == 0 -> currentItems[1].second.position - 1.0
            newIndex >= currentItems.size - 1 -> currentItems[currentItems.size - 2].second.position + 1.0
            else -> {
                val prevPos = currentItems[newIndex - 1].second.position
                val nextPos = currentItems[newIndex + 1].second.position
                (prevPos + nextPos) / 2.0
            }
        }
        repository.modifyItem(listId, categoryId, itemId, mapOf("position" to newPos))
    }
}