package com.example.restaurantapp

import java.util.Date
data class Order(
    val id: String? = null,
    val tableId: String = "",
    val items: List<OrderedItem>? = null,
    val totalAmount: Double = 0.0,
    val completed: Boolean = false,
    val cookingComplete: Boolean = false,
    val timestamp: Date? = null,
    val paymentMethod: String = ""
)

