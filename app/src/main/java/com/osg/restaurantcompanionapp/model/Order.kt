package com.osg.restaurantcompanionapp.model

data class Order(
    val id: Int,
    val orderTime: String,
    val status: Status
)

enum class Status(val status: String) {
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED")
}