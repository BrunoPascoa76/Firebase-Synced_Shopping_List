package com.bruno.shoppinglist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bruno.shoppinglist.viewmodels.AllListsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.bruno.shoppinglist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController = rememberNavController(),
    viewModel: AllListsViewModel = hiltViewModel(),
) {
    val ids by viewModel.shoppingListIds.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

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
                SmallFloatingActionButton(
                    onClick = { /* Trigger QR Scanner and call viewModel.importList(id) */ },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Import List")
                }

                // Create List Button
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "New List")
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

    var showRenameDialog by remember { mutableStateOf(false) }

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
            Text(
                text = shoppingList?.name ?: "...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )


            Row {
                IconButton(
                    onClick = { showRenameDialog = true }
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