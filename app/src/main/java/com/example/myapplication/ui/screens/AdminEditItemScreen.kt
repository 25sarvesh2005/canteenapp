package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.FirebaseViewModel
import com.example.myapplication.data.FoodItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditItemScreen(navController: NavController, itemId: String?, viewModel: FirebaseViewModel) {
    val foodItems by viewModel.foodItems.collectAsState()
    val itemToEdit = foodItems.find { it.id == itemId }

    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var description by remember { mutableStateOf(itemToEdit?.description ?: "") }
    var price by remember { mutableStateOf(itemToEdit?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: "") }
    var imageUrl by remember { mutableStateOf(itemToEdit?.imageUrl ?: "") }

    // Update state when itemToEdit is found or changes
    LaunchedEffect(itemToEdit) {
        itemToEdit?.let {
            name = it.name
            description = it.description
            price = it.price.toString()
            category = it.category
            imageUrl = it.imageUrl
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Item") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    itemToEdit?.let {
                        val updatedItem = it.copy(
                            name = name,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = category,
                            imageUrl = imageUrl
                        )
                        viewModel.updateFoodItem(updatedItem)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
