package com.osg.restaurantcompanionapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.osg.restaurantcompanionapp.model.MenuItem
import com.osg.restaurantcompanionapp.model.OrderItem
import com.osg.restaurantcompanionapp.viewmodel.MenuItemViewModel
import com.osg.restaurantcompanionapp.viewmodel.OrderItemViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemToOrderView(
    orderId: Long,
    menuItemViewModel: MenuItemViewModel,
    orderItemViewModel: OrderItemViewModel,
    onOrderItemAdded: () -> Unit
) {
    val menuItems by menuItemViewModel.menuItemsLiveData.observeAsState()
    val createOrderItemResult by orderItemViewModel.createOrderItemResult.observeAsState()

    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var specialInstructions by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        menuItemViewModel.fetchMenuItems()
    }

    LaunchedEffect(createOrderItemResult) {
        if (createOrderItemResult != null) {
            isLoading = false
            selectedMenuItem = null
            quantity = "1"
            specialInstructions = ""
            onOrderItemAdded()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Add Menu Item to Order",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Dropdown para seleccionar el menu item
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedMenuItem?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Menu Item") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                enabled = !isLoading
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                menuItems?.forEach { menuItem ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = menuItem.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", menuItem.price)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            selectedMenuItem = menuItem
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de cantidad
        OutlinedTextField(
            value = quantity,
            onValueChange = { newValue ->
                quantity = newValue
                quantityError = when {
                    newValue.isBlank() -> "Quantity cannot be empty"
                    newValue.toIntOrNull() == null -> "Quantity must be a number"
                    newValue.toInt() <= 0 -> "Quantity must be greater than 0"
                    else -> null
                }
            },
            label = { Text("Quantity") },
            placeholder = { Text("1") },
            singleLine = true,
            isError = quantityError != null,
            supportingText = {
                if (quantityError != null) {
                    Text(text = quantityError!!)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de instrucciones especiales
        OutlinedTextField(
            value = specialInstructions,
            onValueChange = { specialInstructions = it },
            label = { Text("Special Instructions") },
            placeholder = { Text("e.g., No salt, extra cheese...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de guardar
        Button(
            onClick = {
                val selectedItem = selectedMenuItem
                val qty = quantity.toIntOrNull()

                if (selectedItem != null && qty != null && qty > 0) {
                    isLoading = true
                    val orderItem = OrderItem(
                        orderId = orderId,
                        menuItemId = selectedItem.id,
                        menuItemName = selectedItem.name,
                        menuItemPrice = selectedItem.price,
                        quantity = qty,
                        specialInstructions = specialInstructions.trim(),
                        subtotal = selectedItem.price * qty
                    )
                    orderItemViewModel.createOrderItem(orderItem)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading &&
                     selectedMenuItem != null &&
                     quantityError == null &&
                     quantity.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Add to Order")
            }
        }
    }
}