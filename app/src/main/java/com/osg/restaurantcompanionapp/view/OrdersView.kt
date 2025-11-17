package com.osg.restaurantcompanionapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.osg.restaurantcompanionapp.model.Order
import com.osg.restaurantcompanionapp.model.Status
import com.osg.restaurantcompanionapp.viewmodel.OrderViewModel

@Composable
fun OrdersView(viewModel: OrderViewModel) {
    val orders by viewModel.ordersLiveData.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchActiveOrders()
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
                        OrderListItem(order = order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderListItem(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = "Time: ${order.orderTime}",
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
