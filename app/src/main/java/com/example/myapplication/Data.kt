package com.example.myapplication.data

import java.text.SimpleDateFormat
import java.util.Date

data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = ""
)

data class CartItem(
    val foodItem: FoodItem = FoodItem(),
    val quantity: Int = 0
)

data class Order(
    val orderId: String = "",
    val studentId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val timestamp: String = SimpleDateFormat.getDateTimeInstance().format(Date()),
    var status: String = "Pending",
    val customRequest: String? = null
)
