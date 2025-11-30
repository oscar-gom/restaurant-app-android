package com.osg.restaurantcompanionapp.view

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.osg.restaurantcompanionapp.view.component.DeleteConfirmationDialog
import com.osg.restaurantcompanionapp.model.Order
import com.osg.restaurantcompanionapp.model.Status
import com.osg.restaurantcompanionapp.network.ORDERS_TOPIC
import com.osg.restaurantcompanionapp.network.WS_URL
import com.osg.restaurantcompanionapp.viewmodel.OrderViewModel
import com.osg.restaurantcompanionapp.viewmodel.WebSocketViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersView(viewModel: OrderViewModel, navController: NavController) {
    val orders by viewModel.ordersLiveData.observeAsState()
    val showAddScreen by viewModel.showAddScreen.observeAsState(false)
    val showActiveOnly = rememberSaveable { mutableStateOf(true) }
    val deleteOrderResult by viewModel.deleteOrderResult.observeAsState()
    val orderPendingDeletion = remember { mutableStateOf<Order?>(null) }

    val webSocketViewModel: WebSocketViewModel = viewModel()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchActiveOrders()

        webSocketViewModel.initialize(
            orderViewModel = viewModel,
            wsUrl = WS_URL,
            topic = ORDERS_TOPIC
        )
    }

    LaunchedEffect(showActiveOnly.value) {
        if (showActiveOnly.value) {
            viewModel.fetchActiveOrders()
        } else {
            viewModel.fetchOrders()
        }
    }

    LaunchedEffect(showAddScreen) {
        if (showAddScreen) {
            scope.launch {
                sheetState.show()
            }
            viewModel.onAddScreenShown()
        }
    }

    // Refrescar lista tras una eliminación
    LaunchedEffect(deleteOrderResult) {
        deleteOrderResult?.let { success ->
            if (success) {
                if (showActiveOnly.value) {
                    viewModel.fetchActiveOrders()
                } else {
                    viewModel.fetchOrders()
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = !showActiveOnly.value,
                    onClick = { showActiveOnly.value = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Todos")
                }
                SegmentedButton(
                    selected = showActiveOnly.value,
                    onClick = { showActiveOnly.value = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Activos")
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    orders == null -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    orders!!.isEmpty() -> {
                        Text(
                            text = if (showActiveOnly.value) "No hay órdenes activas" else "No hay órdenes",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(orders!!) { order ->
                                OrderListItem(
                                    order = order,
                                    onClick = {
                                        navController.navigate("orderDetail/${order.id}")
                                    },
                                    onDelete = { orderToDelete ->
                                        orderPendingDeletion.value = orderToDelete
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.onAdd() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add order"
            )
        }
    }

    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }
            },
            sheetState = sheetState
        ) {
            CreateOrderView(
                viewModel = viewModel,
                onOrderCreated = {
                    scope.launch {
                        sheetState.hide()
                        viewModel.fetchActiveOrders()
                    }
                }
            )
        }
    }

    orderPendingDeletion.value?.let { orderToDelete ->
        DeleteConfirmationDialog(
            title = "Delete order",
            message = "Are you sure you want to delete order #${orderToDelete.id}? This action cannot be undone.",
            onConfirm = {
                viewModel.deleteOrder(orderToDelete.id.toInt())
                orderPendingDeletion.value = null
            },
            onDismiss = {
                orderPendingDeletion.value = null
            }
        )
    }
}

@Composable
fun OrderListItem(order: Order, onClick: () -> Unit, onDelete: (Order) -> Unit) {
    val formattedTime = LocalDateTime.parse(order.orderTime)
        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy – HH:mm:ss"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Order #${order.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    StatusChip(status = order.status)
                }
                TextButton(onClick = { onDelete(order) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete order",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Table: ${order.tableNumber}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Time: $formattedTime",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusChip(status: Status) {
    val (backgroundColor, textColor) = when (status) {
        Status.PENDING ->
            Pair(
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.onTertiaryContainer
            )

        Status.COMPLETED ->
            Pair(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer
            )

        Status.CANCELLED ->
            Pair(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer
            )
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = when (status) {
                Status.PENDING -> Status.PENDING.status
                Status.COMPLETED -> Status.COMPLETED.status
                Status.CANCELLED -> Status.CANCELLED.status
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
fun CreateOrderView(
    viewModel: OrderViewModel,
    onOrderCreated: () -> Unit
) {
    val tableNum = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val createOrderResult by viewModel.createOrderResult.observeAsState()

    LaunchedEffect(createOrderResult) {
        if (createOrderResult != null) {
            isLoading.value = false
            tableNum.value = ""
            onOrderCreated()
            viewModel.resetCreateOrderResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Create New Order",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tableNum.value,
            onValueChange = { tableNum.value = it },
            label = { Text("Table Number") },
            placeholder = { Text("123") },
            prefix = { Text("Nº ") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading.value
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (tableNum.value.isNotBlank()) {
                    isLoading.value = true
                    val newOrder = Order(
                        id = 0,
                        tableNumber = tableNum.value.toInt(),
                        status = Status.PENDING
                    )
                    viewModel.createOrder(newOrder)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading.value && tableNum.value.isNotBlank()
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create Order")
            }
        }
    }
}
