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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.restaurantapp.ui.theme.RestaurantAppTheme
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun OrderScreen(navController: NavHostController) {
    val firestoreDb = FirebaseFirestore.getInstance()
    var menuItems by remember { mutableStateOf(emptyList<MenuItem>()) }
    var selectedItems by remember { mutableStateOf(mutableMapOf<String, Int>()) }

    DisposableEffect(Unit) {
        val listenerRegistration = firestoreDb.collection("menu_items")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val fetchedItems = snapshot.documents.mapNotNull { document ->
                        document.toObject(MenuItem::class.java)?.copy(id = document.id)
                    }
                    menuItems = fetchedItems
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
                text = "รับออเดอร์",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(menuItems) { item ->
                OrderMenuItemRow(
                    item = item,
                    quantity = selectedItems[item.id] ?: 0,
                    onItemClick = {
                        val currentQuantity = selectedItems[item.id] ?: 0
                        selectedItems = selectedItems.toMutableMap().apply {
                            put(item.id!!, currentQuantity + 1)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val orderedItems = selectedItems.mapNotNull { (itemId, quantity) ->
                    val menuItem = menuItems.find { it.id == itemId }
                    menuItem?.let {
                        OrderedItem(
                            itemId = it.id!!,
                            itemName = it.name,
                            quantity = quantity,
                            price = it.price
                        )
                    }
                }

                if (orderedItems.isNotEmpty()) {
                    val totalAmount = orderedItems.sumOf { it.price * it.quantity }
                    val newOrder = Order(
                        tableId = "โต๊ะ 1",
                        items = orderedItems,
                        totalAmount = totalAmount
                    )

                    firestoreDb.collection("orders").add(newOrder)
                        .addOnSuccessListener {
                            selectedItems = mutableMapOf()
                        }
                        .addOnFailureListener {
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ยืนยันออเดอร์")
        }
    }
}

@Composable
fun OrderMenuItemRow(item: MenuItem, quantity: Int, onItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "${item.price} บาท", style = MaterialTheme.typography.bodySmall)
        }
        Text(text = "จำนวน: $quantity", style = MaterialTheme.typography.bodyLarge)
    }
}