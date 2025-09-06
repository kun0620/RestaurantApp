package com.example.restaurantapp

data class Table(
    val id: String? = null,
    val tableNumber: Int = 0,
    val status: String = "",
    val tableId: String = "" // เพิ่มบรรทัดนี้
)