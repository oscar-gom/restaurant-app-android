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
import com.osg.restaurantcompanionapp.util.CurrencyFormatter
import com.osg.restaurantcompanionapp.viewmodel.MenuItemViewModel
import java.text.NumberFormat
import java.util.Locale

private data class MenuItemFormState(
    val name: MutableState<String>,
    val description: MutableState<String>,
    val price: MutableState<String>,
    val originalName: MutableState<String>,
    val originalDescription: MutableState<String>,
    val originalPrice: MutableState<String>,
    val isLoading: MutableState<Boolean>,
    val isInitialized: MutableState<Boolean>
) {
    val hasChanges: State<Boolean>
        @Composable get() = remember {
            derivedStateOf {
                name.value != originalName.value ||
                description.value != originalDescription.value ||
                price.value != originalPrice.value
            }
        }

    fun initializeFrom(menuItem: MenuItem) {
        name.value = menuItem.name
        description.value = menuItem.description
        price.value = menuItem.price.toString()

        originalName.value = menuItem.name
        originalDescription.value = menuItem.description
        originalPrice.value = menuItem.price.toString()

        isInitialized.value = true
    }

    fun isValid(): Boolean {
        val priceValue = price.value.toDoubleOrNull()
        return name.value.isNotBlank() &&
               description.value.isNotBlank() &&
               price.value.isNotBlank() &&
               priceValue != null &&
               priceValue > 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDetailView(
    menuItemId: Long,
    navController: NavController,
    viewModel: MenuItemViewModel = viewModel()
) {
    val menuItem by viewModel.menuItemLiveData.observeAsState()
    val updateMenuItemResult by viewModel.updateMenuItemResult.observeAsState()

    val formState = remember {
        MenuItemFormState(
            name = mutableStateOf(""),
            description = mutableStateOf(""),
            price = mutableStateOf(""),
            originalName = mutableStateOf(""),
            originalDescription = mutableStateOf(""),
            originalPrice = mutableStateOf(""),
            isLoading = mutableStateOf(false),
            isInitialized = mutableStateOf(false)
        )
    }

    // Effects
    MenuItemDetailEffects(
        menuItemId = menuItemId,
        menuItem = menuItem,
        updateMenuItemResult = updateMenuItemResult,
        formState = formState,
        viewModel = viewModel,
        navController = navController
    )

    Scaffold(
        topBar = { MenuItemDetailTopBar(navController) }
    ) { paddingValues ->
        MenuItemDetailContent(
            modifier = Modifier.padding(paddingValues),
            menuItem = menuItem,
            formState = formState,
            viewModel = viewModel,
            menuItemId = menuItemId
        )
    }
}

@Composable
private fun MenuItemDetailEffects(
    menuItemId: Long,
    menuItem: MenuItem?,
    updateMenuItemResult: MenuItem?,
    formState: MenuItemFormState,
    viewModel: MenuItemViewModel,
    navController: NavController
) {
    LaunchedEffect(menuItemId) {
        viewModel.fetchMenuItemById(menuItemId.toInt())
    }

    LaunchedEffect(menuItem) {
        if (menuItem != null && !formState.isInitialized.value) {
            formState.initializeFrom(menuItem)
        }
    }

    LaunchedEffect(updateMenuItemResult) {
        if (updateMenuItemResult != null) {
            formState.isLoading.value = false
            viewModel.resetUpdateMenuItemResult()
            navController.popBackStack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItemDetailTopBar(navController: NavController) {
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

@Composable
private fun MenuItemDetailContent(
    modifier: Modifier = Modifier,
    menuItem: MenuItem?,
    formState: MenuItemFormState,
    viewModel: MenuItemViewModel,
    menuItemId: Long
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            menuItem == null && !formState.isInitialized.value -> {
                LoadingIndicator()
            }

            menuItem == null && formState.isInitialized.value -> {
                ErrorMessage("Menu item not found")
            }

            else -> {
                MenuItemEditForm(
                    formState = formState,
                    viewModel = viewModel,
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
private fun MenuItemEditForm(
    formState: MenuItemFormState,
    viewModel: MenuItemViewModel,
    menuItemId: Long
) {
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

        NameTextField(
            value = formState.name.value,
            onValueChange = { formState.name.value = it },
            enabled = !formState.isLoading.value
        )

        DescriptionTextField(
            value = formState.description.value,
            onValueChange = { formState.description.value = it },
            enabled = !formState.isLoading.value
        )

        PriceTextField(
            value = formState.price.value,
            onValueChange = { formState.price.value = it },
            enabled = !formState.isLoading.value
        )

        Spacer(modifier = Modifier.weight(1f))

        UpdateButton(
            formState = formState,
            viewModel = viewModel,
            menuItemId = menuItemId
        )

        if (!formState.hasChanges.value) {
            NoChangesMessage()
        }
    }
}

@Composable
private fun NameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Name") },
        placeholder = { Text("Pizza Margherita") },
        singleLine = true,
        maxLines = 1,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )
}

@Composable
private fun DescriptionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Description") },
        placeholder = { Text("Classic pizza with tomato and mozzarella") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
        enabled = enabled
    )
}

@Composable
private fun PriceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    val currencySymbol = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).currency?.symbol ?: "$"
    }

    val isError = value.isNotEmpty() &&
                  (value.toDoubleOrNull() == null || value.toDoubleOrNull()!! <= 0)

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onValueChange(newValue)
                }
            },
            label = { Text("Price") },
            placeholder = { Text("12.50") },
            prefix = { Text("$currencySymbol ") },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError
        )

        if (isError) {
            Text(
                text = "Price must be a valid number greater than 0",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun UpdateButton(
    formState: MenuItemFormState,
    viewModel: MenuItemViewModel,
    menuItemId: Long
) {
    Button(
        onClick = {
            handleUpdateMenuItem(formState, viewModel, menuItemId)
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
            Text("Update Menu Item")
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

private fun handleUpdateMenuItem(
    formState: MenuItemFormState,
    viewModel: MenuItemViewModel,
    menuItemId: Long
) {
    if (!formState.isValid()) return

    val priceValue = formState.price.value.toDoubleOrNull() ?: return

    formState.isLoading.value = true
    val updatedMenuItem = MenuItem(
        id = menuItemId,
        name = formState.name.value.trim(),
        description = formState.description.value.trim(),
        price = priceValue
    )
    viewModel.updateMenuItem(menuItemId.toInt(), updatedMenuItem)
}

