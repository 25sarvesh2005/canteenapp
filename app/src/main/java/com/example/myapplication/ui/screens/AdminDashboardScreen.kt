package com.example.myapplication.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.myapplication.FirebaseViewModel

@Composable
fun AdminDashboardScreen(navController: NavController, viewModel: FirebaseViewModel) {
    Text(text = "Admin Dashboard")
}
