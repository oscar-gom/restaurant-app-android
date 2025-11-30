package com.osg.restaurantcompanionapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osg.restaurantcompanionapp.model.Order
import com.osg.restaurantcompanionapp.model.OrderItem
import com.osg.restaurantcompanionapp.network.StompWebSocketManager
import kotlinx.coroutines.launch

class WebSocketViewModel : ViewModel() {
    private var orderStompManager: StompWebSocketManager<Order>? = null
    private var orderItemStompManager: StompWebSocketManager<OrderItem>? = null

    fun initializeOrders(orderViewModel: OrderViewModel, wsUrl: String, topic: String) {
        viewModelScope.launch {
            try {
                orderStompManager = StompWebSocketManager(
                    clazz = Order::class.java,
                    onMessageReceived = { order ->
                        orderViewModel.addOrUpdateOrder(order)
                    }
                )
                orderStompManager?.connect(wsUrl, topic)
            } catch (_: Exception) {
            }
        }
    }

    fun initializeOrderItems(orderItemViewModel: OrderItemViewModel, wsUrl: String, topic: String, orderId: Int) {
        viewModelScope.launch {
            try {
                orderItemStompManager = StompWebSocketManager(
                    clazz = OrderItem::class.java,
                    onMessageReceived = { orderItem ->
                        if (orderItem.orderId == orderId.toLong()) {
                            orderItemViewModel.fetchOrderItemsByOrderId(orderId)
                        }
                    }
                )
                orderItemStompManager?.connect(wsUrl, topic)
            } catch (_: Exception) {
            }
        }
    }

    fun disconnectOrderItems() {
        viewModelScope.launch {
            try {
                orderItemStompManager?.disconnect()
                orderItemStompManager = null
            } catch (_: Exception) {
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try {
                orderStompManager?.disconnect()
                orderItemStompManager?.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}