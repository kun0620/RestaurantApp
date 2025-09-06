package com.example.restaurantapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment

@Composable
fun KitchenScreen(navController: NavHostController) {
    val firestoreDb = FirebaseFirestore.getInstance()
    var pendingOrders by remember { mutableStateOf(emptyList<Order>()) }

    // Real-time listener สำหรับออเดอร์ที่ยังไม่ได้ทำ
    DisposableEffect(Unit) {
        val listenerRegistration = firestoreDb.collection("orders")
            .whereEqualTo("cookingComplete", false) // ดึงเฉพาะออเดอร์ที่ครัวยังไม่ทำ
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val fetchedOrders = snapshot.documents.mapNotNull { document ->
                        document.toObject(Order::class.java)?.copy(id = document.id)
                    }
                    pendingOrders = fetchedOrders
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
                text = "รายการอาหารสำหรับครัว",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (pendingOrders.isEmpty()) {
            Text("ไม่มีออเดอร์ที่ต้องทำ", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn {
                items(pendingOrders) { order ->
                    OrderCard(order = order, onComplete = {
                        // อัปเดตสถานะออเดอร์เมื่อทำเสร็จแล้ว
                        firestoreDb.collection("orders").document(order.id!!)
                            .update("cookingComplete", true)
                    })
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onComplete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("โต๊ะที่: ${order.tableId}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            order.items?.forEach { orderedItem ->
                Text(
                    text = "- ${orderedItem.itemName} x${orderedItem.quantity}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                Text("ทำเสร็จแล้ว")
            }
        }
    }
}