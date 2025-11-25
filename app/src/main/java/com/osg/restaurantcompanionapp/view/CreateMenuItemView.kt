package com.osg.restaurantcompanionapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.osg.restaurantcompanionapp.model.MenuItem
import com.osg.restaurantcompanionapp.viewmodel.MenuItemViewModel

@Composable
fun CreateMenuItemView(
    viewModel: MenuItemViewModel,
    onMenuItemCreated: () -> Unit
) {
    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val createMenuItemResult by viewModel.createMenuItemResult.observeAsState()

    LaunchedEffect(createMenuItemResult) {
        if (createMenuItemResult != null) {
            isLoading.value = false
            name.value = ""
            description.value = ""
            price.value = ""
            onMenuItemCreated()
            viewModel.resetCreateMenuItemResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Create New Menu Item",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = price.value,
            onValueChange = { price.value = it },
            label = { Text("Price") },
            placeholder = { Text("12.50") },
            prefix = { Text("$ ") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading.value
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.value.isNotBlank() &&
                    description.value.isNotBlank() &&
                    price.value.isNotBlank()) {

                    val priceValue = price.value.toDoubleOrNull()
                    if (priceValue != null && priceValue > 0) {
                        isLoading.value = true
                        val newMenuItem = MenuItem(
                            id = 0,
                            name = name.value,
                            description = description.value,
                            price = priceValue
                        )
                        viewModel.createMenuItem(newMenuItem)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading.value &&
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
                Text("Create Menu Item")
            }
        }
    }
}