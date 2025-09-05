package com.example.restaurantapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SalesReportScreen(navController: NavHostController) {
    val firestoreDb = FirebaseFirestore.getInstance()
    var orders by remember { mutableStateOf(emptyList<Order>()) }

    DisposableEffect(Unit) {
        val listenerRegistration = firestoreDb.collection("orders")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val fetchedOrders = snapshot.documents.mapNotNull { document ->
                        document.toObject(Order::class.java)?.copy(id = document.id)
                    }
                    orders = fetchedOrders
                }
            }
        onDispose { listenerRegistration.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ย้อนกลับ")
            }
            Text(
                text = "รายงานยอดขาย",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        val totalSales = orders.sumOf { it.totalAmount }
        Text(
            "ยอดขายรวมทั้งหมด: ${String.format("%.2f", totalSales)} บาท",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(orders) { order ->
                OrderSummaryCard(order = order)
            }
        }
    }
}

@Composable
fun OrderSummaryCard(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "ออเดอร์หมายเลข: ${order.id}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "โต๊ะที่: ${order.tableId}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "ยอดรวม: ${String.format("%.2f", order.totalAmount)} บาท",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("รายการที่สั่ง:", style = MaterialTheme.typography.labelLarge)

            order.items?.forEach { orderedItem ->
                Text(
                    text = "- ${orderedItem.itemName} x${orderedItem.quantity} (${String.format("%.2f", orderedItem.price * orderedItem.quantity)} บาท)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}