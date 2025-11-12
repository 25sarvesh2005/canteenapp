package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun LoginScreen(
    navController: NavHostController,
    onStudentLogin: (String, String) -> Unit,
    onAdminLogin: (String, String) -> Unit,
    viewModel: FirebaseViewModel
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                    if (selectedRole == null) {
                        Text("Welcome to the Canteen!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Please select your role to continue", fontSize = 16.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(onClick = { selectedRole = "student" }) {
                                Text("I am a Student")
                            }
                            Button(onClick = { selectedRole = "admin" }) {
                                Text("I am an Admin")
                            }
                        }
                    } else {
                        Text(if (selectedRole == "student") "Student Login" else "Admin Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(if (selectedRole == "student") "Email" else "Username") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            if (selectedRole == "student") {
                                onStudentLogin(email, password)
                            } else {
                                onAdminLogin(email, password)
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Login")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedRole == "student") {
                            TextButton(onClick = { navController.navigate("register") }) {
                                Text("Don't have an account? Register")
                            }
                        }
                        TextButton(onClick = { selectedRole = null }) {
                            Text("Back to role selection")
                        }
                    }
                }
            }
        }
    }
}