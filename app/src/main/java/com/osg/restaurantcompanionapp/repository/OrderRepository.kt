package com.osg.restaurantcompanionapp.repository

import com.osg.restaurantcompanionapp.model.Order
import com.osg.restaurantcompanionapp.network.ApiService

class OrderRepository(private val apiService: ApiService) {
    suspend fun getOrders(): List<Order>? {
       val response = apiService.getOrders()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getActiveOrders(): List<Order>? {
        val response = apiService.getActiveOrders()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getOrderById(id: Int): Order? {
        val response = apiService.getOrderById(id)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun createOrder(order: Order): Order? {
        val response = apiService.createOrder(order)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateOrder(id: Int, order: Order): Order? {
        val response = apiService.updateOrder(id, order)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun deleteOrder(id: Int): Boolean {
        val response = apiService.deleteOrder(id)
        return response.isSuccessful
    }

}