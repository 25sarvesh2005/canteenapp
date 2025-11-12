package com.example.myapplication.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun AdminProfileScreen(navController: NavController, onLogout: () -> Unit) {
    Text(text = "Admin Profile")
}
