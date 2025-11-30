package com.osg.restaurantcompanionapp.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.osg.restaurantcompanionapp.model.OrderItem
import com.osg.restaurantcompanionapp.model.Status
import com.osg.restaurantcompanionapp.viewmodel.MenuItemViewModel
import com.osg.restaurantcompanionapp.viewmodel.OrderItemViewModel
import com.osg.restaurantcompanionapp.viewmodel.OrderViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailView(
    orderId: Int,
    navController: NavController,
    orderViewModel: OrderViewModel = viewModel(),
    orderItemViewModel: OrderItemViewModel = viewModel(),
    menuItemViewModel: MenuItemViewModel = viewModel()
) {
    val order by orderViewModel.orderLiveData.observeAsState()
    val orderItems by orderItemViewModel.orderItemsByOrderIdLiveData.observeAsState()
    val isLoading by orderItemViewModel.isLoading.observeAsState(false)
    val errorMessage by orderItemViewModel.errorMessage.observeAsState()
    val updateOrderResult by orderViewModel.updateOrderResult.observeAsState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(orderId, refreshTrigger) {
        orderViewModel.fetchOrderById(orderId)
    }

    LaunchedEffect(order) {
        order?.let {
            orderItemViewModel.fetchOrderItemsByOrderId(it.id.toInt())
        }
    }

    LaunchedEffect(updateOrderResult) {
        updateOrderResult?.let { _ ->
            refreshTrigger++
            orderViewModel.resetUpdateOrderResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (order != null) "#${order!!.id} – Table ${order!!.tableNumber}"
                        else "Loading..."
                    )
                },
                actions = {
                    order?.let { currentOrder ->
                        Box {
                            TextButton(onClick = { showStatusMenu = true }) {
                                Text(
                                    text = currentOrder.status.status,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change status"
                                )
                            }
                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false }
                            ) {
                                Status.entries.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.status) },
                                        onClick = {
                                            val updatedOrder = currentOrder.copy(status = status)
                                            orderViewModel.updateOrder(currentOrder.id.toInt(), updatedOrder)
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddSheet = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add menu item to order"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Error desconocido",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                orderItems.isNullOrEmpty() -> {
                    Text(
                        text = "No hay items en este pedido",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    OrderItemsList(
                        orderItems = orderItems!!,
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
            },
            sheetState = sheetState
        ) {
            order?.let { currentOrder ->
                AddMenuItemToOrderView(
                    orderId = currentOrder.id,
                    existingOrderItems = orderItems ?: emptyList(),
                    menuItemViewModel = menuItemViewModel,
                    orderItemViewModel = orderItemViewModel,
                    onOrderItemAdded = {
                        scope.launch {
                            sheetState.hide()
                            showAddSheet = false
                            orderItemViewModel.fetchOrderItemsByOrderId(currentOrder.id.toInt())
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OrderItemsList(
    orderItems: List<OrderItem>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orderItems) { orderItem ->
            OrderItemCard(
                orderItem = orderItem,
                navController = navController
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            TotalCard(total = orderItems.sumOf { it.subtotal })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItemCard(
    orderItem: OrderItem,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            navController.navigate("editOrderItem/${orderItem.orderId}/${orderItem.menuItemId}")
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = orderItem.menuItemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quantity: ${orderItem.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Price: $${
                            String.format(
                                Locale.US,
                                "%.2f",
                                orderItem.menuItemPrice
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$${String.format(Locale.US, "%.2f", orderItem.subtotal)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (orderItem.specialInstructions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Instructions: ${orderItem.specialInstructions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TotalCard(total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$${String.format(Locale.US, "%.2f", total)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

