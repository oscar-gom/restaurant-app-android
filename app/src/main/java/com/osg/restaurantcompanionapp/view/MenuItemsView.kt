package com.osg.restaurantcompanionapp.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.osg.restaurantcompanionapp.view.component.DeleteConfirmationDialog
import com.osg.restaurantcompanionapp.view.component.InfoDialog
import com.osg.restaurantcompanionapp.view.component.LoadingDialog
import com.osg.restaurantcompanionapp.model.MenuItem
import com.osg.restaurantcompanionapp.ui.theme.minimalistCardElevation
import com.osg.restaurantcompanionapp.util.CurrencyFormatter
import com.osg.restaurantcompanionapp.viewmodel.MenuItemViewModel
import com.osg.restaurantcompanionapp.viewmodel.OrderItemViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemsView(viewModel: MenuItemViewModel, navController: NavController) {
    // State
    val menuItems by viewModel.menuItemsLiveData.observeAsState()
    val showAddScreen by viewModel.showAddScreen.observeAsState(false)
    val deleteMenuItemResult by viewModel.deleteMenuItemResult.observeAsState()
    val itemPendingDeletion = remember { mutableStateOf<MenuItem?>(null) }

    val orderItemViewModel: OrderItemViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val orderItemsByMenuItemId by orderItemViewModel.orderItemsByMenuItemIdLiveData.observeAsState()

    // Delete flow flags
    val isCheckingUsage = remember { mutableStateOf(false) }
    val cannotDeleteReason = remember { mutableStateOf<String?>(null) }
    val usageCheckMenuItemId = remember { mutableStateOf<Long?>(null) }
    val usageRequestToken = remember { mutableStateOf<Long?>(null) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.fetchMenuItems() }

    LaunchedEffect(showAddScreen) {
        if (showAddScreen) {
            scope.launch { sheetState.show() }
            viewModel.onAddScreenShown()
        }
    }

    LaunchedEffect(deleteMenuItemResult) {
        if (deleteMenuItemResult == true) {
            viewModel.fetchMenuItems()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        when {
            menuItems == null -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            menuItems!!.isEmpty() -> Text("No menu items available", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.bodyLarge)
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(menuItems!!) { menuItem ->
                    MenuItemItem(
                        menuItem = menuItem,
                        onClick = { navController.navigate("menuItemDetail/${menuItem.id}") },
                        onDelete = { item ->
                            isCheckingUsage.value = true
                            cannotDeleteReason.value = null
                            itemPendingDeletion.value = item
                            usageCheckMenuItemId.value = item.id
                            usageRequestToken.value = System.nanoTime()
                            orderItemViewModel.resetOrderItemsByMenuItemId()
                            orderItemViewModel.fetchOrderItemsByMenuItemId(item.id.toInt())
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.onAdd() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
                focusedElevation = 2.dp,
                hoveredElevation = 3.dp
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add menu item")
        }
    }

    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = { scope.launch { sheetState.hide() } },
            sheetState = sheetState
        ) {
            CreateMenuItemView(
                viewModel = viewModel,
                onMenuItemCreated = {
                    scope.launch {
                        sheetState.hide()
                        viewModel.fetchMenuItems()
                    }
                }
            )
        }
    }

    LaunchedEffect(orderItemsByMenuItemId, isCheckingUsage.value, itemPendingDeletion.value, usageCheckMenuItemId.value, usageRequestToken.value) {
        if (isCheckingUsage.value && itemPendingDeletion.value != null && usageCheckMenuItemId.value == itemPendingDeletion.value!!.id && orderItemsByMenuItemId != null) {
            isCheckingUsage.value = false
            val orderIds = orderItemsByMenuItemId?.filter { it.menuItemId == usageCheckMenuItemId.value }?.map { it.orderId }?.distinct() ?: emptyList()
            cannotDeleteReason.value = orderIds.takeIf { it.isNotEmpty() }?.joinToString(prefix = "Cannot delete. Used in orders: ", separator = ", ")
        }
    }

    when {
        isCheckingUsage.value && itemPendingDeletion.value != null -> LoadingDialog(
            title = "Checking usage",
            message = "Verifying...",
            onDismiss = {
                isCheckingUsage.value = false
                usageCheckMenuItemId.value = null
                usageRequestToken.value = null
                itemPendingDeletion.value = null
            }
        )

        cannotDeleteReason.value != null && itemPendingDeletion.value != null -> InfoDialog(
            title = "Cannot delete",
            message = cannotDeleteReason.value ?: "",
            onDismiss = {
                itemPendingDeletion.value = null
                cannotDeleteReason.value = null
                usageCheckMenuItemId.value = null
                usageRequestToken.value = null
            }
        )

        itemPendingDeletion.value != null && !isCheckingUsage.value && cannotDeleteReason.value == null -> {
            val item = itemPendingDeletion.value!!
            if (usageCheckMenuItemId.value == item.id) {
                DeleteConfirmationDialog(
                    title = "Delete menu item",
                    message = "Delete '${item.name}'?",
                    onConfirm = {
                        viewModel.deleteMenuItem(item.id.toInt())
                        itemPendingDeletion.value = null
                    },
                    onDismiss = {
                        itemPendingDeletion.value = null
                        usageCheckMenuItemId.value = null
                        usageRequestToken.value = null
                    }
                )
            }
        }
    }
}

@Composable
fun MenuItemItem(menuItem: MenuItem, onClick: () -> Unit, onDelete: (MenuItem) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = minimalistCardElevation(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(menuItem.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = CurrencyFormatter.format(menuItem.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(0.dp))
                    TextButton(onClick = { onDelete(menuItem) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete menu item", tint = Color(0xFFEF5350))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = menuItem.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}