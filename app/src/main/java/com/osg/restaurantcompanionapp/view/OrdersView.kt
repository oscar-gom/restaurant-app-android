package com.osg.restaurantcompanionapp.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.osg.restaurantcompanionapp.model.Order
import com.osg.restaurantcompanionapp.model.Status
import com.osg.restaurantcompanionapp.network.WS_URL
import com.osg.restaurantcompanionapp.network.ORDERS_TOPIC
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


    LaunchedEffect(showAddScreen) {
        if (showAddScreen) {
            scope.launch {
                sheetState.show()
            }
            viewModel.onAddScreenShown()
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
                    text = "No hay órdenes activas",
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
                            }
                        )
                    }
                }
            }
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
}

@Composable
fun OrderListItem(order: Order, onClick: () -> Unit) {
    val formattedTime = LocalDateTime.parse(order.orderTime)
        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy – HH:mm:ss"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                Text(
                    text = "Order #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = order.status)
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
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer
            )

        Status.COMPLETED ->
            Pair(
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.onTertiaryContainer
            )

        Status.CANCELLED ->
            Pair(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant
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
