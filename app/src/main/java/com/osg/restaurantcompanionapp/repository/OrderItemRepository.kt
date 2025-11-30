package com.osg.restaurantcompanionapp.repository

import com.osg.restaurantcompanionapp.model.OrderItem
import com.osg.restaurantcompanionapp.network.ApiService

class OrderItemRepository(val apiService: ApiService) {
    suspend fun getOrderItems(): List<OrderItem>? {
        val response = apiService.getOrderItems()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getOrderItemByIds(orderId: Int, menuItemId: Int): OrderItem? {
        val response = apiService.getOrderItemByIds(orderId, menuItemId)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getOrderItemsByOrderId(orderId: Int): List<OrderItem>? {
        val response = apiService.getOrderItemsByOrderId(orderId)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getOrderItemsByMenuItemId(menuItemId: Int): List<OrderItem>? {
        val response = apiService.getOrderItemsByMenuItemId(menuItemId)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun createOrderItem(orderItem: OrderItem): OrderItem? {
        val response = apiService.createOrderItem(orderItem)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateOrderItem(orderId: Int, menuItemId: Int, orderItem: OrderItem): OrderItem? {
        val response = apiService.updateOrderItem(orderId, menuItemId, orderItem)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteOrderItem(orderId: Int, menuItemId: Int): Boolean {
        val response = apiService.deleteOrderItem(orderId, menuItemId)
        return response.isSuccessful
    }

    suspend fun deleteAllOrderItems(): String? {
        val response = apiService.deleteAllOrderItems()
        return if (response.isSuccessful) {
            response.body()?.string()
        } else null
    }
}