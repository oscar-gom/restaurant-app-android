package com.osg.restaurantcompanionapp.model

data class OrderItem(
    val menuItemId: Int,
    val menuItemName: String,
    val menuItemPrice: Double,
    val orderId: Int,
    val quantity: Int,
    val specialInstructions: String,
    val subtotal: Double
)