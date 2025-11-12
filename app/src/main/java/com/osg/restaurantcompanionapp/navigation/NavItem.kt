package com.osg.restaurantcompanionapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Orders : NavItem("orders", "Orders", Icons.Default.Face)
    object MenuItem : NavItem("menuItem", "Menu Items", Icons.Default.ShoppingCart)
    object Settings : NavItem("settings", "Settings", Icons.Default.Settings)

    companion object {
        val all = listOf(Orders, MenuItem, Settings)
    }
}