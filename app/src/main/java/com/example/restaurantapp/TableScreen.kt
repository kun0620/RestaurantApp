package com.example.restaurantapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@Composable
fun TableScreen(navController: NavHostController) {
    val firestoreDb = FirebaseFirestore.getInstance()
    var tables by remember { mutableStateOf(emptyList<Table>()) }

    DisposableEffect(Unit) {
        val listenerRegistration = firestoreDb.collection("tables")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val fetchedTables = snapshot.documents.mapNotNull { document ->
                        document.toObject(Table::class.java)?.copy(id = document.id)
                    }
                    tables = fetchedTables
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
                text = "จัดการโต๊ะ",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tables) { table ->
                TableCard(table = table)
            }
        }
    }
}

@Composable
fun TableCard(table: Table) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("โต๊ะที่ ${table.tableNumber}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            val statusColor = if (table.status == "ว่าง") Color.Green else Color.Red
            Text(
                text = table.status,
                style = MaterialTheme.typography.titleMedium,
                color = statusColor
            )
        }
    }
}