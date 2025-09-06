package com.example.restaurantapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@Composable
fun PaymentScreen(navController: NavHostController, tableId: String) {
    val firestoreDb = FirebaseFirestore.getInstance()
    var tableOrders by remember { mutableStateOf(emptyList<Order>()) }
    var tableInfo by remember { mutableStateOf<Table?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("") }

    // ดึงข้อมูลโต๊ะ
    DisposableEffect(tableId) {
        val listenerRegistration = firestoreDb.collection("tables").document(tableId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    tableInfo = snapshot.toObject(Table::class.java)?.copy(id = snapshot.id)
                }
            }
        onDispose { listenerRegistration.remove() }
    }

    // ดึงออเดอร์ทั้งหมดของโต๊ะที่ยังไม่เสร็จ
    DisposableEffect(tableInfo) {
        val listenerRegistration = tableInfo?.let { table ->
            firestoreDb.collection("orders")
                .whereEqualTo("tableId", table.tableId)
                .whereEqualTo("completed", false)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val fetchedOrders = snapshot.documents.mapNotNull { document ->
                            document.toObject(Order::class.java)?.copy(id = document.id)
                        }
                        tableOrders = fetchedOrders
                    }
                }
        }
        onDispose { listenerRegistration?.remove() }
    }

    val totalAmount = tableOrders.sumOf { it.totalAmount }

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
                text = "ชำระเงิน",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (tableInfo != null) {
            Text("โต๊ะที่: ${tableInfo!!.tableNumber}", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("รายการออเดอร์ที่ยังไม่ชำระ:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tableOrders) { order ->
                OrderSummaryCard(order = order)
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "ยอดรวมทั้งหมด: ${String.format(Locale.getDefault(),"%.2f", totalAmount)} บาท",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("ช่องทางการชำระเงิน", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { selectedPaymentMethod = "เงินสด" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPaymentMethod == "เงินสด") MaterialTheme.colorScheme.primary else Color.LightGray
                )
            ) {
                Text("เงินสด")
            }
            Button(
                onClick = { selectedPaymentMethod = "โอนเงิน" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPaymentMethod == "โอนเงิน") MaterialTheme.colorScheme.primary else Color.LightGray
                )
            ) {
                Text("โอนเงิน")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedPaymentMethod.isNotEmpty()) {
                    val batch = firestoreDb.batch()

                    // 1. อัปเดตสถานะของทุกออเดอร์ในโต๊ะให้เป็น completed และเพิ่ม paymentMethod
                    tableOrders.forEach { order ->
                        order.id?.let {
                            val orderRef = firestoreDb.collection("orders").document(it)
                            batch.update(orderRef, "completed", true, "paymentMethod", selectedPaymentMethod)
                        }
                    }

                    // 2. เปลี่ยนสถานะโต๊ะเป็น "ว่าง"
                    tableInfo?.id?.let {
                        val tableRef = firestoreDb.collection("tables").document(it)
                        batch.update(tableRef, "status", "ว่าง")
                    }

                    // 3. Commit batch
                    batch.commit()
                        .addOnSuccessListener {
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            // จัดการข้อผิดพลาดที่นี่
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ยืนยันการชำระเงินและปิดโต๊ะ")
        }
    }
}