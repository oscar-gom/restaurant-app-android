package com.osg.restaurantcompanionapp.viewmodel

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

    fun fetchOrderById(id: Int) {
        viewModelScope.launch {
            val order = _orderRepository.getOrderById(id)
            _orderLiveData.postValue(order)
        }
    }

    fun createOrder(order: Order) {
        viewModelScope.launch {
            val result = _orderRepository.createOrder(order)
            _createOrderResult.postValue(result)
        }
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
}