package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CartItem
import com.example.myapplication.data.FoodItem
import com.example.myapplication.data.Order
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

sealed interface AuthState {
    object Authenticated : AuthState
    object AdminAuthenticated : AuthState
    object EmailNotVerified : AuthState
    data class Error(val message: String) : AuthState
    object Loading : AuthState
    object Unauthenticated : AuthState
}

class FirebaseViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems = _foodItems.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders = _orders.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    private val _dailySales = MutableStateFlow(0.0)
    val dailySales = _dailySales.asStateFlow()

    private val _weeklySales = MutableStateFlow(0.0)
    val weeklySales = _weeklySales.asStateFlow()

    init {
        fetchFoodItems()
        fetchOrders()
        // Temporary migration function - you can remove this after it has run once.
        // migrateFoodItemIds()
    }
    
    fun migrateFoodItemIds() {
        viewModelScope.launch {
            val foodItemsCollection = db.collection("foodItems").get().await()
            val foodItems = foodItemsCollection.toObjects<FoodItem>()
            
            // Ensure "Veg Noodles" is included if it's missing
            val hasVegNoodles = foodItems.any { it.name == "Veg Noodles" }
            if (!hasVegNoodles) {
                foodItems.toMutableList().add(FoodItem(name = "Veg Noodles", description = "Delicious veg noodles", price = 70.0, category = "Lunch"))
            }

            val updatedItems = foodItems.mapIndexed { index, item ->
                item.copy(id = (index + 1).toString())
            }

            // Write the updated items back to Firestore
            updatedItems.forEach {
                db.collection("foodItems").document(it.id).set(it).await()
            }
        }
    }

    fun registerStudent(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                auth.currentUser?.sendEmailVerification()?.await()
                _authState.value = AuthState.EmailNotVerified // User needs to verify email
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun loginStudent(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                if (auth.currentUser?.isEmailVerified == true) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.EmailNotVerified
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun loginAdmin(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            if (email == "stark3y37@gmail.com" && password == "123456") {
                _authState.value = AuthState.AdminAuthenticated
            } else {
                _authState.value = AuthState.Error("Invalid admin credentials.")
            }
        }
    }

    fun resetAuthState() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun getCurrentUser() = auth.currentUser

    private fun fetchFoodItems() {
        db.collection("foodItems").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                val items = it.toObjects<FoodItem>()
                _foodItems.value = items.sortedBy { item -> item.id.toIntOrNull() ?: 0 }
            }
        }
    }

    private fun fetchOrders() {
        db.collection("orders").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                val orders = it.toObjects<Order>()
                _orders.value = orders
                calculateSales(orders)
            }
        }
    }

    private fun calculateSales(orders: List<Order>) {
        val now = Calendar.getInstance()
        _dailySales.value = orders.filter { isSameDay(it.timestamp, now) }.sumOf { it.totalPrice }
        _weeklySales.value = orders.filter { isSameWeek(it.timestamp, now) }.sumOf { it.totalPrice }
    }

    private fun isSameDay(orderTimestamp: String, cal: Calendar): Boolean {
        val sdf = SimpleDateFormat.getDateTimeInstance()
        val date = try { sdf.parse(orderTimestamp) } catch (e: Exception) { null } ?: return false
        val orderCal = Calendar.getInstance().apply { time = date }
        return orderCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
                orderCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
    }

    private fun isSameWeek(orderTimestamp: String, cal: Calendar): Boolean {
        val sdf = SimpleDateFormat.getDateTimeInstance()
        val date = try { sdf.parse(orderTimestamp) } catch (e: Exception) { null } ?: return false
        val orderCal = Calendar.getInstance().apply { time = date }
        return orderCal.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR) &&
                orderCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
    }

    fun addFoodItem(item: FoodItem) {
        viewModelScope.launch {
            val maxId = _foodItems.value.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0
            val newId = (maxId + 1).toString()
            val newItem = item.copy(id = newId)
            db.collection("foodItems").document(newItem.id).set(newItem).await()
        }
    }

    fun updateFoodItem(item: FoodItem) {
        viewModelScope.launch {
            db.collection("foodItems").document(item.id).set(item).await()
        }
    }

    fun deleteFoodItem(item: FoodItem) {
        viewModelScope.launch {
            db.collection("foodItems").document(item.id).delete().await()
        }
    }

    suspend fun addOrder(order: Order, studentId: String): String {
        val date = SimpleDateFormat("ddMMyy").format(Date())
        val itemId = if (order.items.size == 1) order.items.first().foodItem.id else "00"
        val paddedItemId = itemId.padStart(2, '0')
        val prefix = "$date$paddedItemId"

        val querySnapshot = db.collection("orders")
            .whereGreaterThanOrEqualTo("orderId", prefix)
            .whereLessThan("orderId", prefix + '\uf8ff')
            .get()
            .await()

        val orderCountForToday = querySnapshot.size()
        val orderNumber = (orderCountForToday + 1).toString().padStart(2, '0')

        val newOrderId = "$prefix$orderNumber"
        val newOrder = order.copy(orderId = newOrderId, studentId = studentId)
        db.collection("orders").document(newOrder.orderId).set(newOrder).await()
        return newOrder.orderId
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch {
            db.collection("orders").document(order.orderId).set(order).await()
        }
    }

    fun addToCart(item: FoodItem) {
        val currentCart = _cart.value.toMutableList()
        val existingItem = currentCart.find { it.foodItem.id == item.id }

        if (existingItem != null) {
            val itemIndex = currentCart.indexOf(existingItem)
            currentCart[itemIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentCart.add(CartItem(item, 1))
        }
        _cart.value = currentCart
    }

    fun clearCart() {
        _cart.value = emptyList()
    }
}
