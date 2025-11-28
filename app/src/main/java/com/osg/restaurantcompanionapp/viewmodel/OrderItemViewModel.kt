package com.osg.restaurantcompanionapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.osg.restaurantcompanionapp.model.OrderItem
import com.osg.restaurantcompanionapp.repository.OrderItemRepository
import com.osg.restaurantcompanionapp.network.ApiService
import com.osg.restaurantcompanionapp.network.retrofit
import kotlinx.coroutines.launch

class OrderItemViewModel : ViewModel() {
    private val _orderItemRepository = OrderItemRepository(retrofit.create(ApiService::class.java))

    private val _orderItemsLiveData = MutableLiveData<List<OrderItem>?>()
    val orderItemsLiveData: LiveData<List<OrderItem>?> = _orderItemsLiveData

    private val _orderItemLiveData = MutableLiveData<OrderItem?>()
    val orderItemLiveData: LiveData<OrderItem?> = _orderItemLiveData

    private val _orderItemsByOrderIdLiveData = MutableLiveData<List<OrderItem>?>()
    val orderItemsByOrderIdLiveData: LiveData<List<OrderItem>?> = _orderItemsByOrderIdLiveData

    private val _orderItemsByMenuItemIdLiveData = MutableLiveData<List<OrderItem>?>()
    val orderItemsByMenuItemIdLiveData: LiveData<List<OrderItem>?> = _orderItemsByMenuItemIdLiveData

    private val _createOrderItemResult = MutableLiveData<OrderItem?>()
    val createOrderItemResult: LiveData<OrderItem?> = _createOrderItemResult

    private val _updateOrderItemResult = MutableLiveData<OrderItem?>()
    val updateOrderItemResult: LiveData<OrderItem?> = _updateOrderItemResult

    private val _deleteOrderItemResult = MutableLiveData<Boolean>()
    val deleteOrderItemResult: LiveData<Boolean> = _deleteOrderItemResult

    private val _showAddScreen = MutableLiveData<Boolean>()
    val showAddScreen: LiveData<Boolean> = _showAddScreen

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun onAdd() {
        _showAddScreen.value = true
    }

    fun onAddScreenShown() {
        _showAddScreen.value = false
    }

    fun fetchOrderItems() {
        viewModelScope.launch {
            val orderItems = _orderItemRepository.getOrderItems()
            _orderItemsLiveData.postValue(orderItems)
        }
    }


    fun fetchOrderItemByIds(orderId: Int, menuItemId: Int) {
        viewModelScope.launch {
            val orderItem = _orderItemRepository.getOrderItemByIds(orderId, menuItemId)
            _orderItemLiveData.postValue(orderItem)
        }
    }

    fun fetchOrderItemsByOrderId(orderId: Int) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null)
            try {
                val orderItems = _orderItemRepository.getOrderItemsByOrderId(orderId)
                if (orderItems != null) {
                    _orderItemsByOrderIdLiveData.postValue(orderItems)
                } else {
                    _errorMessage.postValue("Cant load order items for order ID $orderId")
                }
            } catch (exception: Exception) {
                _errorMessage.postValue(exception.message ?: "Unknown error")
                _orderItemsByOrderIdLiveData.postValue(emptyList())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchOrderItemsByMenuItemId(menuItemId: Int) {
        viewModelScope.launch {
            val orderItems = _orderItemRepository.getOrderItemsByMenuItemId(menuItemId)
            _orderItemsByMenuItemIdLiveData.postValue(orderItems)
        }
    }

    fun createOrderItem(orderItem: OrderItem) {
        viewModelScope.launch {
            val result = _orderItemRepository.createOrderItem(orderItem)
            _createOrderItemResult.postValue(result)
        }
    }

    fun updateOrderItem(orderId: Int, menuItemId: Int, orderItem: OrderItem) {
        viewModelScope.launch {
            val result = _orderItemRepository.updateOrderItem(orderId, menuItemId, orderItem)
            _updateOrderItemResult.postValue(result)
        }
    }

    fun resetUpdateOrderItemResult() {
        _updateOrderItemResult.value = null
    }

    fun deleteOrderItem(orderId: Int, menuItemId: Int) {
        viewModelScope.launch {
            val result = _orderItemRepository.deleteOrderItem(orderId, menuItemId)
            _deleteOrderItemResult.postValue(result)
        }
    }
}