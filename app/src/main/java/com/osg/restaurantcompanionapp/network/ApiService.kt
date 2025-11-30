package com.osg.restaurantcompanionapp.network

import com.osg.restaurantcompanionapp.model.MenuItem
import com.osg.restaurantcompanionapp.model.Order
import com.osg.restaurantcompanionapp.model.OrderItem
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


private const val baseUrl = "http://192.168.1.161:8080"

// URL de WebSocket STOMP
const val WS_URL = "ws://192.168.1.161:8080/ws"
const val ORDERS_TOPIC = "/topic/orders"

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface ApiService {
    // Orders
    @GET("/api/orders")
    suspend fun getOrders(): Response<List<Order>>

    @GET("/api/orders/active")
    suspend fun getActiveOrders(): Response<List<Order>>

    @GET("/api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: Int): Response<Order>

    @POST("/api/orders/create")
    suspend fun createOrder(@Body order: Order): Response<Order>

    @PUT("/api/orders/update/{id}")
    suspend fun updateOrder(@Path("id") id: Int, @Body order: Order): Response<Order>

    @DELETE("/api/orders/delete/{id}")
    suspend fun deleteOrder(@Path("id") id: Int): Response<Unit>

    @DELETE("/api/orders/delete-all")
    suspend fun deleteAllOrders(): Response<String>

    // Menu Items
    @GET("/api/menu-items")
    suspend fun getMenuItems(): Response<List<MenuItem>>

    @GET("/api/menu-items/{id}")
    suspend fun getMenuItemById(@Path("id") id: Int): Response<MenuItem>

    @POST("/api/menu-items/create")
    suspend fun createMenuItem(@Body menuItem: MenuItem): Response<MenuItem>

    @PUT("/api/menu-items/update/{id}")
    suspend fun updateMenuItem(@Path("id") id: Int, @Body menuItem: MenuItem): Response<MenuItem>

    @DELETE("/api/menu-items/delete/{id}")
    suspend fun deleteMenuItem(@Path("id") id: Int): Response<Unit>

    @DELETE("/api/menu-items/delete-all")
    suspend fun deleteAllMenuItems(): Response<String>

    // Order Items
    @GET("/api/order-items")
    suspend fun getOrderItems(): Response<List<OrderItem>>

    @GET("/api/order-items/{orderId}/{menuItemId}")
    suspend fun getOrderItemByIds(
        @Path("orderId") orderId: Int,
        @Path("menuItemId") menuItemId: Int
    ): Response<OrderItem>

    @GET("/api/order-items/order/{orderId}")
    suspend fun getOrderItemsByOrderId(@Path("orderId") orderId: Int): Response<List<OrderItem>>

    @GET("/api/order-items/menu-item/{menuItemId}")
    suspend fun getOrderItemsByMenuItemId(@Path("menuItemId") menuItemId: Int): Response<List<OrderItem>>

    @POST("/api/order-items/create")
    suspend fun createOrderItem(@Body orderItem: OrderItem): Response<OrderItem>


    @PUT("/api/order-items/update/{orderId}/{menuItemId}")
    suspend fun updateOrderItem(
        @Path("orderId") orderId: Int,
        @Path("menuItemId") menuItemId: Int,
        @Body orderItem: OrderItem
    ): Response<OrderItem>

    @DELETE("/api/order-items/delete/{orderId}/{menuItemId}")
    suspend fun deleteOrderItem(
        @Path("orderId") orderId: Int,
        @Path("menuItemId") menuItemId: Int
    ): Response<Unit>

    @DELETE("/api/order-items/delete-all")
    suspend fun deleteAllOrderItems(): Response<String>
}