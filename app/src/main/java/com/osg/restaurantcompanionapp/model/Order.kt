package com.osg.restaurantcompanionapp.model

import java.time.LocalDateTime

data class Order(
    val id: Long,
    val orderTime: String = LocalDateTime.now().toString(),
    val status: Status,
    val tableNumber: Int
)

enum class Status(val status: String) {
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED")
}