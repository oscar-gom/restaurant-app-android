package com.osg.restaurantcompanionapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.osg.restaurantcompanionapp.model.Order
import com.osg.restaurantcompanionapp.repository.OrderRepository
import com.osg.restaurantcompanionapp.network.ApiService
import com.osg.restaurantcompanionapp.network.retrofit
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val _orderRepository = OrderRepository(retrofit.create(ApiService::class.java))

    private val _ordersLiveData = MutableLiveData<List<Order>?>()
    val ordersLiveData: LiveData<List<Order>?> = _ordersLiveData

    private val _orderLiveData = MutableLiveData<Order?>()
    val orderLiveData: LiveData<Order?> = _orderLiveData

    private val _createOrderResult = MutableLiveData<Order?>()
    val createOrderResult: LiveData<Order?> = _createOrderResult

    private val _createOrderError = MutableLiveData<String?>()
    val createOrderError: LiveData<String?> = _createOrderError

    private val _updateOrderResult = MutableLiveData<Order?>()
    val updateOrderResult: LiveData<Order?> = _updateOrderResult

    private val _deleteOrderResult = MutableLiveData<Boolean>()
    val deleteOrderResult: LiveData<Boolean> = _deleteOrderResult

    private val _showAddScreen = MutableLiveData<Boolean>()
    val showAddScreen: LiveData<Boolean> = _showAddScreen

    fun onAdd() {
        _showAddScreen.value = true
    }

    fun onAddScreenShown() {
        _showAddScreen.value = false
    }

    fun fetchOrders() {
        viewModelScope.launch {
            val orders = _orderRepository.getOrders()
            _ordersLiveData.postValue(orders)
        }
    }

    fun fetchActiveOrders() {
        viewModelScope.launch {
            val orders = _orderRepository.getActiveOrders()
            _ordersLiveData.postValue(orders)
        }
    }

    fun fetchOrderById(id: Int) {
        viewModelScope.launch {
            val order = _orderRepository.getOrderById(id)
            _orderLiveData.postValue(order)
        }
    }

    fun createOrder(order: Order) {
        viewModelScope.launch {
            _createOrderError.postValue(null)
            _createOrderResult.postValue(null)
            try {
                val result = _orderRepository.createOrder(order)
                if (result != null) {
                    _createOrderResult.postValue(result)
                } else {
                    _createOrderError.postValue("No se pudo crear la orden. Verifica la conexión o los datos.")
                }
            } catch (e: Exception) {
                _createOrderError.postValue(e.message ?: "Error inesperado al crear la orden")
            }
        }
    }

    fun resetCreateOrderResult() {
        _createOrderResult.postValue(null)
    }

    fun resetCreateOrderError() {
        _createOrderError.postValue(null)
    }

    fun updateOrder(id: Int, order: Order) {
        viewModelScope.launch {
            val result = _orderRepository.updateOrder(id, order)
            _updateOrderResult.postValue(result)
        }
    }

    fun deleteOrder(id: Int) {
        viewModelScope.launch {
            val result = _orderRepository.deleteOrder(id)
            _deleteOrderResult.postValue(result)
        }
    }

    /**
     * Añade o actualiza un order en la lista actual
     * Este método es llamado cuando se recibe un nuevo order por WebSocket
     */
    fun addOrUpdateOrder(order: Order) {
        val currentOrders = _ordersLiveData.value?.toMutableList() ?: mutableListOf()

        // Buscar si el order ya existe en la lista
        val existingIndex = currentOrders.indexOfFirst { it.id == order.id }

        if (existingIndex != -1) {
            // Actualizar el order existente
            currentOrders[existingIndex] = order
            Log.d("OrderViewModel", "Order actualizado: ${order.id}")
        } else {
            // Añadir el nuevo order al inicio de la lista
            currentOrders.add(0, order)
            Log.d("OrderViewModel", "Nuevo order añadido: ${order.id}")
        }

        // Actualizar el LiveData en el hilo principal
        _ordersLiveData.postValue(currentOrders)
    }
}