package com.osg.restaurantcompanionapp.model

data class Order(
    val id: Long,
    val orderTime: String,
    val status: Status,
    val tableNumber: Int
)

enum class Status(val status: String) {
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED")
}