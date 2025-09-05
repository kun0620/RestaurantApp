package com.example.restaurantapp

data class Table(
    var id: String? = null,
    var tableNumber: Int = 0,
    var status: String = "ว่าง" // "ว่าง" หรือ "ไม่ว่าง"
)