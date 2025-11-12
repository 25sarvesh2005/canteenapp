package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.AdminAddItemScreen
import com.example.myapplication.ui.screens.AdminDashboardScreen
import com.example.myapplication.ui.screens.AdminEditItemScreen
import com.example.myapplication.ui.screens.AdminItemsScreen
import com.example.myapplication.ui.screens.AdminOrderDetailsScreen
import com.example.myapplication.ui.screens.AdminOrdersScreen
import com.example.myapplication.ui.screens.AdminProfileScreen
import com.example.myapplication.ui.screens.CartScreen
import com.example.myapplication.ui.screens.OrderConfirmationScreen
import com.example.myapplication.ui.screens.OrderTrackingScreen
import com.example.myapplication.ui.screens.ProfileScreen
import com.example.myapplication.ui.screens.StudentHomeScreen
import com.example.myapplication.ui.screens.StudentOrderDetailsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FirebaseViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CanteenApp(viewModel)
        }
    }
}

@Composable
fun CanteenApp(viewModel: FirebaseViewModel) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        NavHost(navController = navController, startDestination = "login", modifier = Modifier.padding(padding)) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    viewModel = viewModel,
                    onStudentLogin = { email, password ->
                        viewModel.loginStudent(email, password)
                    },
                    onAdminLogin = { email, password ->
                        viewModel.loginAdmin(email, password)
                    }
                )
            }
            composable("register") {
                RegistrationScreen(
                    navController = navController,
                    onRegister = { email, password ->
                        viewModel.registerStudent(email, password)
                    }
                )
            }
            composable("student_app") {
                StudentApp(viewModel = viewModel, onLogout = { viewModel.resetAuthState() })
            }
            composable("admin_app") {
                AdminApp(viewModel = viewModel, onLogout = { viewModel.resetAuthState() })
            }
            composable("email_verification") {
                EmailVerificationScreen(onResendEmail = { /* Implement resend email logic */ })
            }
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate("student_app") { popUpTo("login") { inclusive = true } }
            }
            is AuthState.AdminAuthenticated -> {
                navController.navigate("admin_app") { popUpTo("login") { inclusive = true } }
            }
            is AuthState.EmailNotVerified -> {
                navController.navigate("email_verification") { popUpTo("login") { inclusive = true } }
            }
            is AuthState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar((authState as AuthState.Error).message)
                }
            }
            AuthState.Unauthenticated -> {
                if (navController.currentBackStackEntry?.destination?.route != "login") {
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            }
            else -> Unit
        }
    }
}

@Composable
fun StudentApp(viewModel: FirebaseViewModel, onLogout: () -> Unit) {
    val studentNavController = rememberNavController()
    val studentId = viewModel.getCurrentUser()?.uid ?: ""
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = studentNavController.currentDestination?.route == "home",
                    onClick = { studentNavController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart") },
                    selected = studentNavController.currentDestination?.route == "cart",
                    onClick = { studentNavController.navigate("cart") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    selected = studentNavController.currentDestination?.route == "orders",
                    onClick = { studentNavController.navigate("orders") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = studentNavController.currentDestination?.route == "profile",
                    onClick = { studentNavController.navigate("profile") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = studentNavController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                StudentHomeScreen(navController = studentNavController, viewModel = viewModel)
            }
            composable("cart") {
                CartScreen(navController = studentNavController, viewModel = viewModel)
            }
            composable("order_confirmation/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderConfirmationScreen(orderId = orderId, navController = studentNavController)
            }
            composable("orders") {
                OrderTrackingScreen(navController = studentNavController, viewModel = viewModel, studentId = studentId)
            }
            composable("profile") {
                ProfileScreen(navController = studentNavController, onLogout = onLogout)
            }
             composable("student_order_details/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId")
                StudentOrderDetailsScreen(navController = studentNavController, orderId = orderId, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AdminApp(viewModel: FirebaseViewModel, onLogout: () -> Unit) {
    val adminNavController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = adminNavController.currentDestination?.route == "admin_dashboard",
                    onClick = { adminNavController.navigate("admin_dashboard") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Items") },
                    label = { Text("Items") },
                    selected = adminNavController.currentDestination?.route == "admin_items",
                    onClick = { adminNavController.navigate("admin_items") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    selected = adminNavController.currentDestination?.route == "admin_orders",
                    onClick = { adminNavController.navigate("admin_orders") }
                )
                 NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = adminNavController.currentDestination?.route == "admin_profile",
                    onClick = { adminNavController.navigate("admin_profile") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = adminNavController,
            startDestination = "admin_dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("admin_dashboard") {
                AdminDashboardScreen(navController = adminNavController, viewModel = viewModel)
            }
            composable("admin_items") {
                AdminItemsScreen(navController = adminNavController, viewModel = viewModel)
            }
            composable("admin_add_item") {
                AdminAddItemScreen(navController = adminNavController, viewModel = viewModel)
            }
            composable("admin_edit_item/{itemId}") { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")
                AdminEditItemScreen(navController = adminNavController, itemId = itemId, viewModel = viewModel)
            }
            composable("admin_orders") {
                AdminOrdersScreen(navController = adminNavController, viewModel = viewModel)
            }
            composable("admin_order_details/{orderId}") { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId")
                AdminOrderDetailsScreen(navController = adminNavController, orderId = orderId, viewModel = viewModel)
            }
            composable("admin_profile") {
                AdminProfileScreen(navController = adminNavController, onLogout = onLogout)
            }
        }
    }
}

@Composable
fun EmailVerificationScreen(onResendEmail: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Please verify your email address.")
        Spacer(modifier = Modifier.height(16.dp))
        Text("A verification email has been sent to your email address. Please click the link in the email to verify your account.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onResendEmail) {
            Text("Resend Verification Email")
        }
    }
}

@Composable
fun RegistrationScreen(
    navController: NavHostController,
    onRegister: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create a Student Account",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if (password == confirmPassword) {
                            onRegister(email, password)
                        } else {
                            // Show error
                        }
                    }) {
                        Text("Register")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Already have an account? Login")
                    }
                }
            }
        }
    }
}
