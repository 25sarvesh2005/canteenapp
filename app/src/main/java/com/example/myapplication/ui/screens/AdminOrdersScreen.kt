package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.FirebaseViewModel
import com.example.myapplication.data.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(navController: NavController, viewModel: FirebaseViewModel) {
    val orders by viewModel.orders.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val statuses = listOf("Pending", "Delivered", "Canceled")

    val filteredOrders = orders.filter { it.status == statuses[selectedTab] }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Orders") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                statuses.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(filteredOrders) { order ->
                    OrderRow(order = order, onClick = {
                        navController.navigate("admin_order_details/${order.orderId}")
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun OrderRow(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Order ID: ${order.orderId}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Student ID: ${order.studentId}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Total: ${order.totalPrice}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
