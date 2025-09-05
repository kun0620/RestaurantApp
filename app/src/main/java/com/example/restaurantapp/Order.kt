package com.example.restaurantapp

data class Order(
    var id: String? = null,
    var tableId: String = "",
    var items: List<OrderedItem> = listOf(),
    var totalAmount: Double = 0.0,
    var status: String = "รอดำเนินการ" // "รอดำเนินการ", "เสร็จสิ้น", "ชำระเงินแล้ว"
)