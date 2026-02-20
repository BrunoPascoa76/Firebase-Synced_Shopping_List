package com.bruno.shoppinglist.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bruno.shoppinglist.R
import com.bruno.shoppinglist.data.ShoppingListCategory
import com.bruno.shoppinglist.data.ShoppingListItem
import com.bruno.shoppinglist.viewmodels.ListDetailsViewModel
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailsScreen(
    listId: String,
    navController: NavController, // Use the controller directly
    viewModel: ListDetailsViewModel = hiltViewModel()
) {
    val shoppingList by viewModel.getList(listId).collectAsStateWithLifecycle(initialValue = null)
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    if (showAddCategoryDialog) {
        TextDialog(
            title = stringResource(R.string.Dialog_CreateList_Title),
            label = stringResource(R.string.Name_Uppercase),
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                viewModel.createCategory(listId, name)
                showAddCategoryDialog = false
            }
        )
    }

    if (showShareDialog) {
        QRCodeDisplayDialog(listId) { showShareDialog = false }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = shoppingList?.name ?: "Loading...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share List",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val categoryEntries = shoppingList?.categories?.entries?.toList() ?: emptyList()
            if (categoryEntries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.ShoppingBasket,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                stringResource(R.string.ShoppingListDetails_NoCategories),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
            }
            categoryEntries.forEach { categoryEntry ->
                key(categoryEntry.key) {
                    CategoryCard(
                        listId = listId,
                        categoryId = categoryEntry.key,
                        category = categoryEntry.value,
                        viewModel = viewModel
                    )
                }
            }
            AddCategoryButton(onClick = { showAddCategoryDialog = true })
        }
    }
}

@Composable
fun CategoryCard(
    listId: String,
    categoryId: String,
    category: ShoppingListCategory,
    viewModel: ListDetailsViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    var localItems by remember {
        mutableStateOf(category.items.toList().sortedBy { it.second.position })
    }

    LaunchedEffect(category.items) {
        localItems = category.items.toList().sortedBy { it.second.position }
    }

    // 1. Define the Column-based reorderable state
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromItem = localItems.find { it.first == from.key }
        val toItem = localItems.find { it.first == to.key }

        if (fromItem != null && toItem != null) {
            val fromLocalIndex = localItems.indexOf(fromItem)
            val toLocalIndex = localItems.indexOf(toItem)

            localItems = localItems.toMutableList().apply {
                add(toLocalIndex, removeAt(fromLocalIndex))
            }
        }
    }

    if (showAddItemDialog) {
        ItemDialog(
            title = stringResource(R.string.Dialog_CreateList_Title),
            label = stringResource(R.string.Name_Uppercase),
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, quantity ->
                viewModel.createItem(
                    listId,
                    categoryId,
                    name,
                    quantity
                ) // Make sure your ViewModel has this function
                showAddItemDialog = false
            }
        )
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(15.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {viewModel.deleteCategory(listId,categoryId)}
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete category")
                }
            }

            // Items List
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LazyColumn(
                    state=lazyListState,
                    modifier = Modifier
                        .heightIn(max = 2000.dp)
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    userScrollEnabled = false
                ) {
                    if (category.items.isEmpty()) {
                        item{Text(text = stringResource(R.string.Category_Empty))}
                    }
                    items(localItems, key = { it.first }) { (itemId, item) ->
                        ReorderableItem(reorderableState, key = itemId) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                            Surface(
                                shadowElevation = elevation,
                                tonalElevation = 0.dp, // Removed to keep it invisible
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent, // Makes the surface background invisible
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SwipeToDeleteWrapper(onDelete = {
                                    viewModel.deleteItem(
                                        listId,
                                        categoryId,
                                        itemId
                                    )
                                }) {
                                    ShoppingItemRow(
                                        listId = listId,
                                        categoryId = categoryId,
                                        itemId = itemId,
                                        item = item,
                                        viewModel = viewModel,
                                        reorderableScope = this, // Pass the scope for the drag handle
                                        onDragStopped = {
                                            val finalIndex = localItems.indexOfFirst { it.first == itemId }
                                            viewModel.moveItem(listId, categoryId, itemId, finalIndex, localItems)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item{AddItemButton { showAddItemDialog = true }}
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    listId: String,
    categoryId: String,
    itemId: String,
    item: ShoppingListItem,
    viewModel: ListDetailsViewModel,
    reorderableScope: ReorderableCollectionItemScope, // Added Scope
    onDragStopped: () -> Unit // Added Callback
) {
    var isChecked by remember { mutableStateOf(item.purchased) }
    val dismissState = rememberSwipeToDismissBoxState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            viewModel.deleteItem(listId, categoryId, itemId)
        }
    }

    val textColor = if (isChecked) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // Greyed out
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None


    Row(
        modifier = Modifier
            .fillMaxWidth().padding(horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        with(reorderableScope) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reorder",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .draggableHandle(
                        onDragStarted = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDragStopped = onDragStopped
                    )
            )
        }
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.name,
            modifier = Modifier
                .weight(1f)
                .basicMarquee(
                    iterations = Int.MAX_VALUE,    // Loop forever
                    animationMode = MarqueeAnimationMode.Immediately,
                    initialDelayMillis = 1000,            // Pause for 1s before starting/restarting
                    spacing = MarqueeSpacing.fractionOfContainer(1f/3f) // Gap between loops
                ),
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = textDecoration
            ),
            color = textColor
        )

        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                isChecked = !isChecked
                viewModel.toggleItemPurchased(listId, categoryId, itemId, isChecked)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteWrapper(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    // Trigger the delete action when the swipe settles in the 'EndToStart' position
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp)) // Ensures background doesn't leak out of corners
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                // The "Danger Zone" that stays underneath
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        ) {
            // The actual content (your Card or Row)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainer, // Use the "just right" color we found earlier
                shape = RoundedCornerShape(20.dp) // Keeps the corners clean
            ) {
                content()
            }
        }
    }
}

@Composable
fun ItemDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var inputValue by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text(label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    // Show empty string if 0 so the user doesn't have to delete '0' first
                    value = if (quantity == 0) "" else quantity.toString(),
                    onValueChange = { newValue ->
                        // 1. Handle empty input (allows user to clear the field)
                        if (newValue.isBlank()) {
                            quantity = 0
                        }
                        // 2. Only allow digits and prevent leading zeros if you want
                        else if (newValue.all { it.isDigit() }) {
                            // 3. Prevent integer overflow (toIntOrNull returns null if number is too big)
                            newValue.toIntOrNull()?.let {
                                quantity = it
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.Quantity)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done // "Done" button on keyboard instead of "Enter"
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = inputValue.isNotBlank(), // Only allow "OK" if there's text
                onClick = { onConfirm(inputValue, quantity) }
            ) {
                Text(stringResource(R.string.Action_OK))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.Action_Cancel))
            }
        }
    )
}

@Composable
fun AddCategoryButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CreateNewFolder,
            contentDescription = null
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.ShoppingListDetails_CategoryAdd),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun AddItemButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
            contentDescription = null
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.ShoppingListDetails_ItemAdd),
            style = MaterialTheme.typography.labelLarge
        )
    }
}