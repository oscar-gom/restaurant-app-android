package com.osg.restaurantcompanionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.osg.restaurantcompanionapp.navigation.NavItem
import com.osg.restaurantcompanionapp.ui.theme.RestaurantCompanionAppTheme
import com.osg.restaurantcompanionapp.view.EditOrderItemView
import com.osg.restaurantcompanionapp.view.MenuItemDetailView
import com.osg.restaurantcompanionapp.view.MenuItemsView
import com.osg.restaurantcompanionapp.view.OrderDetailView
import com.osg.restaurantcompanionapp.view.OrdersView
import com.osg.restaurantcompanionapp.view.SettingsView
import com.osg.restaurantcompanionapp.view.component.BaseScaffold
import com.osg.restaurantcompanionapp.viewmodel.MenuItemViewModel
import com.osg.restaurantcompanionapp.viewmodel.OrderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestaurantCompanionAppTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NavItem.Orders.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(NavItem.Orders.route) {
            val vm: OrderViewModel = viewModel()
            BaseScaffold(
                navController = navController,
                current = NavItem.Orders,
            ) {
                OrdersView(viewModel = vm, navController = navController)
            }
        }

        composable(
            route = NavItem.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            OrderDetailView(orderId = orderId, navController = navController)
        }

        composable(NavItem.MenuItem.route) {
            val vm: MenuItemViewModel = viewModel()
            BaseScaffold(
                navController = navController,
                current = NavItem.MenuItem,
            ) {
                MenuItemsView(viewModel = vm, navController = navController)
            }
        }

        composable(
            route = NavItem.MenuItemDetail.route,
            arguments = listOf(navArgument("menuItemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val menuItemId = backStackEntry.arguments?.getLong("menuItemId") ?: 0L
            MenuItemDetailView(menuItemId = menuItemId, navController = navController)
        }

        composable(
            route = NavItem.EditOrderItem.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.LongType },
                navArgument("menuItemId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            val menuItemId = backStackEntry.arguments?.getLong("menuItemId") ?: 0L
            EditOrderItemView(
                orderId = orderId,
                menuItemId = menuItemId,
                navController = navController
            )
        }

        composable(NavItem.Settings.route) {
            BaseScaffold(
                navController = navController,
                current = NavItem.Settings
            ) {
                SettingsView()
            }
        }
    }
}