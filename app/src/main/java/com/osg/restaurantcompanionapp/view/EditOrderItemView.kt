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
import com.osg.restaurantcompanionapp.model.OrderItem
import com.osg.restaurantcompanionapp.viewmodel.OrderItemViewModel
import java.util.Locale

private data class OrderItemFormState(
    val quantity: MutableState<String>,
    val specialInstructions: MutableState<String>,
    val originalQuantity: MutableState<String>,
    val originalSpecialInstructions: MutableState<String>,
    val isLoading: MutableState<Boolean>,
    val isInitialized: MutableState<Boolean>
) {
    val hasChanges: State<Boolean>
        @Composable get() = remember {
            derivedStateOf {
                quantity.value != originalQuantity.value ||
                specialInstructions.value != originalSpecialInstructions.value
            }
        }

    fun initializeFrom(orderItem: OrderItem) {
        quantity.value = orderItem.quantity.toString()
        specialInstructions.value = orderItem.specialInstructions

        originalQuantity.value = orderItem.quantity.toString()
        originalSpecialInstructions.value = orderItem.specialInstructions

        isInitialized.value = true
    }

    fun isValid(): Boolean {
        val quantityValue = quantity.value.toIntOrNull()
        return quantity.value.isNotBlank() &&
               quantityValue != null &&
               quantityValue > 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderItemView(
    orderId: Long,
    menuItemId: Long,
    navController: NavController,
    viewModel: OrderItemViewModel = viewModel()
) {
    val orderItem by viewModel.orderItemLiveData.observeAsState()
    val updateOrderItemResult by viewModel.updateOrderItemResult.observeAsState()

    val formState = remember {
        OrderItemFormState(
            quantity = mutableStateOf(""),
            specialInstructions = mutableStateOf(""),
            originalQuantity = mutableStateOf(""),
            originalSpecialInstructions = mutableStateOf(""),
            isLoading = mutableStateOf(false),
            isInitialized = mutableStateOf(false)
        )
    }

    OrderItemEditEffects(
        orderId = orderId,
        menuItemId = menuItemId,
        orderItem = orderItem,
        updateOrderItemResult = updateOrderItemResult,
        formState = formState,
        viewModel = viewModel,
        navController = navController
    )

    Scaffold(
        topBar = { OrderItemEditTopBar(navController) }
    ) { paddingValues ->
        OrderItemEditContent(
            modifier = Modifier.padding(paddingValues),
            orderItem = orderItem,
            formState = formState,
            viewModel = viewModel,
            orderId = orderId,
            menuItemId = menuItemId
        )
    }
}

@Composable
private fun OrderItemEditEffects(
    orderId: Long,
    menuItemId: Long,
    orderItem: OrderItem?,
    updateOrderItemResult: OrderItem?,
    formState: OrderItemFormState,
    viewModel: OrderItemViewModel,
    navController: NavController
) {
    LaunchedEffect(orderId, menuItemId) {
        viewModel.fetchOrderItemByIds(orderId.toInt(), menuItemId.toInt())
    }

    LaunchedEffect(orderItem) {
        if (orderItem != null && !formState.isInitialized.value) {
            formState.initializeFrom(orderItem)
        }
    }

    LaunchedEffect(updateOrderItemResult) {
        if (updateOrderItemResult != null) {
            formState.isLoading.value = false
            viewModel.resetUpdateOrderItemResult()
            navController.popBackStack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderItemEditTopBar(navController: NavController) {
    TopAppBar(
        title = { Text("Edit Order Item") },
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

@Composable
private fun OrderItemEditContent(
    modifier: Modifier = Modifier,
    orderItem: OrderItem?,
    formState: OrderItemFormState,
    viewModel: OrderItemViewModel,
    orderId: Long,
    menuItemId: Long
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            orderItem == null && !formState.isInitialized.value -> {
                LoadingIndicator()
            }

            orderItem == null && formState.isInitialized.value -> {
                ErrorMessage("Order item not found")
            }

            else -> {
                OrderItemEditForm(
                    orderItem = orderItem!!,
                    formState = formState,
                    viewModel = viewModel,
                    orderId = orderId,
                    menuItemId = menuItemId
                )
            }
        }
    }
}

@Composable
private fun BoxScope.LoadingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.align(Alignment.Center)
    )
}

@Composable
private fun BoxScope.ErrorMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier.align(Alignment.Center),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun OrderItemEditForm(
    orderItem: OrderItem,
    formState: OrderItemFormState,
    viewModel: OrderItemViewModel,
    orderId: Long,
    menuItemId: Long
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Order Item",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MenuItemInfoCard(orderItem)

        Spacer(modifier = Modifier.height(8.dp))

        QuantityTextField(
            value = formState.quantity.value,
            onValueChange = { formState.quantity.value = it },
            enabled = !formState.isLoading.value
        )

        SpecialInstructionsTextField(
            value = formState.specialInstructions.value,
            onValueChange = { formState.specialInstructions.value = it },
            enabled = !formState.isLoading.value
        )

        SubtotalDisplay(
            orderItem = orderItem,
            quantity = formState.quantity.value
        )

        Spacer(modifier = Modifier.weight(1f))

        UpdateButton(
            formState = formState,
            viewModel = viewModel,
            orderId = orderId,
            menuItemId = menuItemId,
            orderItem = orderItem
        )

        if (!formState.hasChanges.value) {
            NoChangesMessage()
        }
    }
}

@Composable
private fun MenuItemInfoCard(orderItem: OrderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = orderItem.menuItemName,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Price: $${String.format(Locale.US, "%.2f", orderItem.menuItemPrice)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuantityTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    val isError = value.isNotEmpty() &&
                  (value.toIntOrNull() == null || value.toIntOrNull()!! <= 0)

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                    onValueChange(newValue)
                }
            },
            label = { Text("Quantity") },
            placeholder = { Text("1") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError
        )

        if (isError) {
            Text(
                text = "Quantity must be a number greater than 0",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun SpecialInstructionsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Special Instructions") },
        placeholder = { Text("e.g., No salt, extra cheese...") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
        enabled = enabled
    )
}

@Composable
private fun SubtotalDisplay(
    orderItem: OrderItem,
    quantity: String
) {
    val quantityValue = quantity.toIntOrNull() ?: orderItem.quantity
    val subtotal = orderItem.menuItemPrice * quantityValue

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subtotal",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$${String.format(Locale.US, "%.2f", subtotal)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun UpdateButton(
    formState: OrderItemFormState,
    viewModel: OrderItemViewModel,
    orderId: Long,
    menuItemId: Long,
    orderItem: OrderItem
) {
    Button(
        onClick = {
            handleUpdateOrderItem(formState, viewModel, orderId, menuItemId, orderItem)
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !formState.isLoading.value &&
                  formState.hasChanges.value &&
                  formState.isValid()
    ) {
        if (formState.isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Update Order Item")
        }
    }
}

@Composable
private fun ColumnScope.NoChangesMessage() {
    Text(
        text = "No changes to save",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
}

private fun handleUpdateOrderItem(
    formState: OrderItemFormState,
    viewModel: OrderItemViewModel,
    orderId: Long,
    menuItemId: Long,
    orderItem: OrderItem
) {
    if (!formState.isValid()) return

    val quantityValue = formState.quantity.value.toIntOrNull() ?: return

    formState.isLoading.value = true
    val updatedOrderItem = orderItem.copy(
        quantity = quantityValue,
        specialInstructions = formState.specialInstructions.value.trim(),
        subtotal = orderItem.menuItemPrice * quantityValue
    )
    viewModel.updateOrderItem(orderId.toInt(), menuItemId.toInt(), updatedOrderItem)
}