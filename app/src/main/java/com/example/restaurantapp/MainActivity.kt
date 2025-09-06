package com.example.restaurantapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantapp.ui.theme.RestaurantAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestaurantAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("menuManagement") {
            MenuManagementScreen(navController)
        }
        composable("orderTaking") {
            OrderScreen(navController)
        }
        composable("tableManagement") {
            TableScreen(navController)
        }
        composable("salesReport") {
            SalesReportScreen(navController)
        }
        composable("kitchenScreen") {
            KitchenScreen(navController)
        }
        composable("paymentScreen/{tableId}") { backStackEntry ->
            val tableId = backStackEntry.arguments?.getString("tableId")
            if (tableId != null) {
                PaymentScreen(navController, tableId)
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("menuManagement") }) {
            Text("จัดการเมนู")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("orderTaking") }) {
            Text("รับออเดอร์")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("tableManagement") }) {
            Text("จัดการโต๊ะ")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("salesReport") }) {
            Text("รายงานยอดขาย")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("kitchenScreen") }) {
            Text("หน้าจอครัว")
        }
    }
}