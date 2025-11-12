package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.FirebaseViewModel
import com.example.myapplication.data.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailsScreen(navController: NavController, orderId: String?, viewModel: FirebaseViewModel) {
    val orders by viewModel.orders.collectAsState()
    val order = orders.find { it.orderId == orderId }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Order Details") })
        }
    ) { padding ->
        if (order != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Order ID: ${order.orderId}", style = MaterialTheme.typography.titleLarge)
                Text("Student ID: ${order.studentId}", style = MaterialTheme.typography.bodyMedium)
                Text("Status: ${order.status}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Items:", style = MaterialTheme.typography.titleMedium)
                order.items.forEach {
                    Text("${it.foodItem.name} x ${it.quantity} - Rs.${it.foodItem.price * it.quantity}")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Total Price: Rs.${order.totalPrice}", style = MaterialTheme.typography.titleMedium)

                order.customRequest?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Custom Request: $it", style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                if (order.status == "Pending") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { 
                            val updatedOrder = order.copy(status = "Delivered")
                            viewModel.updateOrder(updatedOrder)
                            navController.popBackStack()
                        }) {
                            Text("Mark as Delivered")
                        }
                        Button(onClick = { 
                            val updatedOrder = order.copy(status = "Canceled")
                            viewModel.updateOrder(updatedOrder)
                            navController.popBackStack()
                        }) {
                            Text("Mark as Canceled")
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Text("Order not found.", modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.Center))
            }
        }
    }
}
