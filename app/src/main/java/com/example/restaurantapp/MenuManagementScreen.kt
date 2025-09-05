package com.example.restaurantapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.restaurantapp.ui.theme.RestaurantAppTheme
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MenuManagementScreen(navController: NavHostController) {
    val firestoreDb = FirebaseFirestore.getInstance()
    var menuName by remember { mutableStateOf("") }
    var menuPrice by remember { mutableStateOf("") }
    var menuItems by remember { mutableStateOf(emptyList<MenuItem>()) }

    // ตัวฟังแบบเรียลไทม์เพื่ออัปเดต UI โดยอัตโนมัติ
    DisposableEffect(Unit) {
        val listenerRegistration = firestoreDb.collection("menu_items")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedItems = snapshot.documents.map { document ->
                        document.toObject(MenuItem::class.java)?.copy(id = document.id)
                    }
                    menuItems = fetchedItems.filterNotNull()
                }
            }
        onDispose { listenerRegistration.remove() }
    }

    // ฟังก์ชันสำหรับลบรายการเมนูจาก Firestore
    fun deleteMenuItem(item: MenuItem) {
        item.id?.let { id ->
            firestoreDb.collection("menu_items").document(id).delete()
        }
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
                text = "จัดการเมนู",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = menuName,
            onValueChange = { menuName = it },
            label = { Text("ชื่อเมนู") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = menuPrice,
            onValueChange = { menuPrice = it },
            label = { Text("ราคา") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (menuName.isNotEmpty() && menuPrice.isNotEmpty()) {
                    val price = menuPrice.toDoubleOrNull()
                    if (price != null) {
                        val menuItem = MenuItem(name = menuName, price = price)
                        firestoreDb.collection("menu_items").add(menuItem)
                        menuName = ""
                        menuPrice = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("เพิ่มเมนู")
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(menuItems) { item ->
                MenuItemRow(item = item, onDeleteClick = {
                    deleteMenuItem(item)
                })
            }
        }
    }
}