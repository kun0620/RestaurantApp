package com.example.restaurantapp

data class MenuItem(
    var id: String? = null,
    var name: String = "",
    var price: Double = 0.0,
    var category: String = ""
)