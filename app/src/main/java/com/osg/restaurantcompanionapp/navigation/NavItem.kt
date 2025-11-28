package com.osg.restaurantcompanionapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Orders : NavItem("orders", "Orders", Icons.AutoMirrored.Filled.List)
    object MenuItem : NavItem("menuItem", "Menu Items", Icons.Default.ShoppingCart)
    object Settings : NavItem("settings", "Settings", Icons.Default.Settings)
    object OrderDetail :
        NavItem("orderDetail/{orderId}", "Order Detail", Icons.AutoMirrored.Filled.List) {
        fun createRoute(orderId: Int) = "orderDetail/$orderId"
    }

    object MenuItemDetail :
        NavItem("menuItemDetail/{menuItemId}", "Menu Item Detail", Icons.Default.ShoppingCart) {
        fun createRoute(menuItemId: Long) = "menuItemDetail/$menuItemId"
    }

    object EditOrderItem :
        NavItem("editOrderItem/{orderId}/{menuItemId}", "Edit Order Item", Icons.AutoMirrored.Filled.List) {
        fun createRoute(orderId: Long, menuItemId: Long) = "editOrderItem/$orderId/$menuItemId"
    }

    companion object {
        val all = listOf(Orders, MenuItem, Settings)
    }
}