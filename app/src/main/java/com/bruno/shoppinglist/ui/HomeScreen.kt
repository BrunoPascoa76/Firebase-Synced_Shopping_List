package com.bruno.shoppinglist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bruno.shoppinglist.R
import com.bruno.shoppinglist.viewmodels.AllListsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController = rememberNavController(),
    viewModel: AllListsViewModel = hiltViewModel(),
) {
    val ids by viewModel.shoppingListIds.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        TextDialog(
            title=stringResource(R.string.Dialog_CreateList_Title),
            label=stringResource(R.string.Name_Uppercase),
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.createList(name) // Make sure your ViewModel has this function
                showAddDialog = false
            }
        )
    }

    if(showImportDialog){
        QrScannerDialog(
            onDismiss = {showImportDialog=false},
            onCodeDetected = {id->viewModel.importList(id)}
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.HomeScreen_Title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // QR Import Button
                FloatingActionButton(
                    onClick = { showImportDialog=true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Import List",modifier = Modifier.fillMaxSize(0.75f))
                }

                // Create List Button
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "New List",modifier = Modifier.fillMaxSize(0.85f))
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
        ){
            if(ids.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd, // Or Icons.Rounded.ShoppingBasket
                        contentDescription = null,
                        modifier = Modifier.size(120.dp), // The "Big" size
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.HomeScreen_NoListsMessage),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }else{
                LazyColumn(
                    modifier=Modifier.fillMaxSize(),
                    contentPadding= PaddingValues(top=8.dp,bottom=80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ids, key = { it }) { id ->
                        ShoppingListRow(
                            listId = id,
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListRow(
    listId: String,
    viewModel: AllListsViewModel = hiltViewModel(),
    navController: NavController= rememberNavController(),
) {
    val shoppingList by remember(listId) {
        viewModel.getList(listId)
    }.collectAsStateWithLifecycle(initialValue = null)

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    ElevatedCard(
        onClick = { navController.navigate("list/$listId") },
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text(stringResource(R.string.Name_Uppercase)) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Row {
                    IconButton(onClick = {
                        viewModel.renameList(listId, editedName)
                        isEditing = false
                    }) {
                        Icon(Icons.Rounded.Check, "Confirm", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { isEditing = false }) {
                        Icon(Icons.Rounded.Close, "Cancel", tint = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Text(
                    text = shoppingList?.name ?: "...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )


                Row {
                    IconButton(
                        onClick = {
                            editedName=shoppingList?.name?:""
                            isEditing = true
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Rename List",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.removeList(listId) }
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete List",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}