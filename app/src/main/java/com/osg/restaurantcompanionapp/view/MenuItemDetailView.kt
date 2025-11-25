package com.osg.restaurantcompanionapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.osg.restaurantcompanionapp.model.MenuItem
import com.osg.restaurantcompanionapp.viewmodel.MenuItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDetailView(
    menuItemId: Long,
    navController: NavController,
    viewModel: MenuItemViewModel = viewModel()
) {
    val menuItem by viewModel.menuItemLiveData.observeAsState()
    val updateMenuItemResult by viewModel.updateMenuItemResult.observeAsState()

    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val isInitialized = remember { mutableStateOf(false) }

    val originalName = remember { mutableStateOf("") }
    val originalDescription = remember { mutableStateOf("") }
    val originalPrice = remember { mutableStateOf("") }

    val hasChanges = remember {
        derivedStateOf {
            name.value != originalName.value ||
                    description.value != originalDescription.value ||
                    price.value != originalPrice.value
        }
    }

    LaunchedEffect(menuItemId) {
        viewModel.fetchMenuItemById(menuItemId.toInt())
    }

    LaunchedEffect(menuItem) {
        if (menuItem != null && !isInitialized.value) {
            name.value = menuItem!!.name
            description.value = menuItem!!.description
            price.value = menuItem!!.price.toString()

            originalName.value = menuItem!!.name
            originalDescription.value = menuItem!!.description
            originalPrice.value = menuItem!!.price.toString()

            isInitialized.value = true
        }
    }

    LaunchedEffect(updateMenuItemResult) {
        if (updateMenuItemResult != null) {
            isLoading.value = false
            viewModel.resetUpdateMenuItemResult()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Item Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                menuItem == null && !isInitialized.value -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                menuItem == null && isInitialized.value -> {
                    Text(
                        text = "Menu item not found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Edit Menu Item",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = name.value,
                            onValueChange = { name.value = it },
                            label = { Text("Name") },
                            placeholder = { Text("Pizza Margherita") },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading.value
                        )

                        OutlinedTextField(
                            value = description.value,
                            onValueChange = { description.value = it },
                            label = { Text("Description") },
                            placeholder = { Text("Classic pizza with tomato and mozzarella") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            enabled = !isLoading.value
                        )

                        OutlinedTextField(
                            value = price.value,
                            onValueChange = { newValue ->
                                // Solo permitir números y punto decimal
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    price.value = newValue
                                }
                            },
                            label = { Text("Price") },
                            placeholder = { Text("12.50") },
                            prefix = { Text("$ ") },
                            singleLine = true,
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading.value,
                            isError = price.value.isNotEmpty() && (price.value.toDoubleOrNull() == null || price.value.toDoubleOrNull()!! <= 0)
                        )

                        if (price.value.isNotEmpty() && (price.value.toDoubleOrNull() == null || price.value.toDoubleOrNull()!! <= 0)) {
                            Text(
                                text = "Price must be a valid number greater than 0",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (name.value.isNotBlank() &&
                                    description.value.isNotBlank() &&
                                    price.value.isNotBlank()
                                ) {
                                    val priceValue = price.value.toDoubleOrNull()
                                    if (priceValue != null && priceValue > 0) {
                                        isLoading.value = true
                                        val updatedMenuItem = MenuItem(
                                            id = menuItemId,
                                            name = name.value.trim(),
                                            description = description.value.trim(),
                                            price = priceValue
                                        )
                                        viewModel.updateMenuItem(
                                            menuItemId.toInt(),
                                            updatedMenuItem
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading.value &&
                                    hasChanges.value &&
                                    name.value.isNotBlank() &&
                                    description.value.isNotBlank() &&
                                    price.value.isNotBlank() &&
                                    price.value.toDoubleOrNull() != null &&
                                    price.value.toDoubleOrNull()!! > 0
                        ) {
                            if (isLoading.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Update Menu Item")
                            }
                        }

                        if (!hasChanges.value) {
                            Text(
                                text = "No changes to save",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}

