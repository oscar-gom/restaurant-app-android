package com.osg.restaurantcompanionapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osg.restaurantcompanionapp.network.StompWebSocketManager
import kotlinx.coroutines.launch

class WebSocketViewModel : ViewModel() {
    private var stompManager: StompWebSocketManager? = null

    fun initialize(orderViewModel: OrderViewModel, wsUrl: String, topic: String) {
        viewModelScope.launch {
            try {
                stompManager = StompWebSocketManager { order ->
                    orderViewModel.addOrUpdateOrder(order)
                }

                stompManager?.connect(wsUrl, topic)
            } catch (_: Exception) {
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try {
                stompManager?.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}