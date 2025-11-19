package com.osg.restaurantcompanionapp.model

data class OrderItem(
    val orderId: Long,
    val menuItemId: Long,
    val menuItemName: String,
    val menuItemPrice: Double,
    val quantity: Int,
    val specialInstructions: String,
    val subtotal: Double
)