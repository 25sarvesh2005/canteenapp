package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.FirebaseViewModel
import com.example.myapplication.R
import com.example.myapplication.data.CartItem
import com.example.myapplication.data.FoodItem
import com.example.myapplication.data.Order
import kotlinx.coroutines.launch

@Composable
fun StudentHomeScreen(
    navController: NavHostController,
    viewModel: FirebaseViewModel
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showSingleOrderDialog by remember { mutableStateOf(false) }
    var selectedItemForOrder by remember { mutableStateOf<FoodItem?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val studentId = viewModel.getCurrentUser()?.uid ?: ""
    val cart by viewModel.cart.collectAsState()

    val categories = listOf("All", "Breakfast", "Lunch", "Snacks", "Beverages")
    val foodItems by viewModel.foodItems.collectAsState()
    val filteredItems = foodItems.filter { item ->
        (selectedCategory == "All" || item.category == selectedCategory) &&
                (searchQuery.isEmpty() || item.name.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text("Hi, Student!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("What would you like to eat today?", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Available Items", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            items(filteredItems) { item ->
                FoodItemCard(
                    item = item,
                    onAddClick = { viewModel.addToCart(item) },
                    onOrderNowClick = {
                        selectedItemForOrder = item
                        showSingleOrderDialog = true
                    }
                )
            }
        }
    }

    if (showSingleOrderDialog && selectedItemForOrder != null) {
        var customRequest by remember { mutableStateOf("") }
        SingleOrderConfirmationDialog(
            item = selectedItemForOrder!!,
            customRequest = customRequest,
            onCustomRequestChange = { customRequest = it },
            onConfirm = {
                coroutineScope.launch {
                    val newOrder = Order(
                        items = listOf(CartItem(selectedItemForOrder!!, 1)),
                        totalPrice = selectedItemForOrder!!.price,
                        customRequest = customRequest.ifBlank { null }
                    )
                    val newOrderId = viewModel.addOrder(newOrder, studentId)
                    showSingleOrderDialog = false
                    selectedItemForOrder = null
                    navController.navigate("order_confirmation/$newOrderId")
                }
            },
            onDismiss = {
                showSingleOrderDialog = false
                selectedItemForOrder = null
            }
        )
    }

    if (cart.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("cart") },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(cart.sumOf { it.quantity }.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SingleOrderConfirmationDialog(
    item: FoodItem,
    customRequest: String,
    onCustomRequestChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Your Order") },
        text = {
            Column {
                Text("You are about to order:")
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.name, fontWeight = FontWeight.Bold)
                Text("Price: ₹${item.price}")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = customRequest,
                    onValueChange = onCustomRequestChange,
                    label = { Text("Special Instructions (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search items...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
fun FoodItemCard(item: FoodItem, onAddClick: () -> Unit, onOrderNowClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop,
                placeholder =  painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_background)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${item.price.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = "Add to cart")
                        }
                        Button(onClick = onOrderNowClick) {
                            Text("Order Now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreen(navController: NavHostController, viewModel: FirebaseViewModel) {
    val cartItems by viewModel.cart.collectAsState()
    val totalPrice = cartItems.sumOf { it.foodItem.price * it.quantity }
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val studentId = viewModel.getCurrentUser()?.uid ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Cart", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty.")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cartItems) { cartItem ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(cartItem.foodItem.name, fontWeight = FontWeight.Bold)
                            Text("Quantity: ${cartItem.quantity}")
                        }
                        Text("₹${cartItem.foodItem.price * cartItem.quantity}")
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Total: ₹$totalPrice", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Place Order")
            }
        }
    }

    if (showDialog) {
        var customRequest by remember { mutableStateOf("") }
        OrderConfirmationDialog(
            customRequest = customRequest,
            onCustomRequestChange = { customRequest = it },
            onConfirm = {
                coroutineScope.launch {
                    val newOrder = Order(
                        items = cartItems,
                        totalPrice = totalPrice,
                        customRequest = customRequest.ifBlank { null }
                    )
                    val newOrderId = viewModel.addOrder(newOrder, studentId)
                    viewModel.clearCart()
                    showDialog = false
                    navController.navigate("order_confirmation/$newOrderId")
                }
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun OrderConfirmationDialog(customRequest: String, onCustomRequestChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Order") },
        text = {
            Column {
                Text("Are you sure you want to place this order?")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = customRequest,
                    onValueChange = onCustomRequestChange,
                    label = { Text("Special Instructions (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun CheckoutScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Checkout Screen")
    }
}

@Composable
fun OrderConfirmationScreen(orderId: String, navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Order Placed Successfully!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Order ID: $orderId")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("home") }) {
                Text("Continue Shopping")
            }
        }
    }
}

@Composable
fun OrderTrackingScreen(navController: NavHostController, viewModel: FirebaseViewModel, studentId: String) {
    val orders by viewModel.orders.collectAsState()
    val studentOrders = orders.filter { it.studentId == studentId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Orders", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (studentOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't placed any orders yet.")
            }
        } else {
            LazyColumn {
                items(studentOrders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { navController.navigate("student_order_details/${order.orderId}") },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Order ID: ${order.orderId}", fontWeight = FontWeight.Bold)
                            Text("Total: ₹${order.totalPrice}")
                            Text("Status: ${order.status}")
                            Text("Placed on: ${order.timestamp}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavHostController, onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}

@Composable
fun StudentOrderDetailsScreen(navController: NavHostController, orderId: String?, viewModel: FirebaseViewModel) {
    val order = viewModel.orders.collectAsState().value.find { it.orderId == orderId }
    var showCancelDialog by remember { mutableStateOf(false) }

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Order not found.")
        }
        return
    }

    val canBeCancelled = order.status == "Pending" || order.status == "Confirmed"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Order Details", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Order ID: ${order.orderId}", fontWeight = FontWeight.Bold)
        Text("Placed on: ${order.timestamp}")
        Text("Status: ${order.status}")

        order.customRequest?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Custom Request:", fontWeight = FontWeight.Bold)
            Text(it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(order.items) { cartItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(cartItem.foodItem.name, fontWeight = FontWeight.Bold)
                        Text("Quantity: ${cartItem.quantity}")
                    }
                    Text("₹${cartItem.foodItem.price * cartItem.quantity}")
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Total: ₹${order.totalPrice}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (canBeCancelled) {
            Button(
                onClick = { showCancelDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancel Order")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Orders")
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Order") },
            text = { Text("Are you sure you want to cancel this order?") },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedOrder = order.copy(status = "Cancelled")
                        viewModel.updateOrder(updatedOrder)
                        showCancelDialog = false
                    }
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                Button(onClick = { showCancelDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
