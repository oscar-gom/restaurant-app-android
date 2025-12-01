package com.osg.restaurantcompanionapp.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.osg.restaurantcompanionapp.view.component.DeleteConfirmationDialog
import com.osg.restaurantcompanionapp.viewmodel.MenuItemViewModel
import com.osg.restaurantcompanionapp.viewmodel.OrderItemViewModel
import com.osg.restaurantcompanionapp.viewmodel.OrderViewModel

@Composable
fun SettingsView(
    orderViewModel: OrderViewModel = viewModel(),
    menuItemViewModel: MenuItemViewModel = viewModel(),
    orderItemViewModel: OrderItemViewModel = viewModel()
) {
    val showDeleteAllDialog = remember { mutableStateOf(false) }
    val deletionStep = remember { mutableStateOf(0) }

    val orderItemsDeleted = orderItemViewModel.deleteAllOrderItemsResult.observeAsState()
    val ordersDeleted = orderViewModel.deleteAllOrdersResult.observeAsState()
    val menuItemsDeleted = menuItemViewModel.deleteAllMenuItemsResult.observeAsState()

    LaunchedEffect(orderItemsDeleted.value) {
        if (deletionStep.value == 1 && orderItemsDeleted.value != null) {
            deletionStep.value = 2
            orderViewModel.deleteAllOrders()
        }
    }

    LaunchedEffect(ordersDeleted.value) {
        if (deletionStep.value == 2 && ordersDeleted.value != null) {
            deletionStep.value = 3
            menuItemViewModel.deleteAllMenuItems()
        }
    }

    LaunchedEffect(menuItemsDeleted.value) {
        if (deletionStep.value == 3 && menuItemsDeleted.value != null) {
            deletionStep.value = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Danger Zone",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFEF5350),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { showDeleteAllDialog.value = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF5350)
            )
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Delete All Data")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This will delete all orders, menu items and order items",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDeleteAllDialog.value) {
        DeleteConfirmationDialog(
            title = "Delete All Data",
            message = "Are you sure you want to delete ALL data (orders, menu items, and order items)? This action cannot be undone.",
            onConfirm = {
                deletionStep.value = 1
                orderItemViewModel.deleteAllOrderItems()
                showDeleteAllDialog.value = false
            },
            onDismiss = {
                showDeleteAllDialog.value = false
            }
        )
    }
}
